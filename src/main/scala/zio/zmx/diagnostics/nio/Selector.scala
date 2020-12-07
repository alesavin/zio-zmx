package zio.zmx.diagnostics.nio

import java.io.IOException
import java.nio.channels.{ClosedSelectorException, Selector => JSelector, SelectionKey => JSelectionKey}

import zio.IO

import scala.jdk.CollectionConverters._

class Selector(val selector: JSelector) {

  val selectedKeys: IO[ClosedSelectorException, Set[SelectionKey]] =
    IO.effect(selector.selectedKeys())
      .map(_.asScala.toSet[JSelectionKey].map(new SelectionKey(_)))
      .refineToOrDie[ClosedSelectorException]

  def removeKey(key: SelectionKey): IO[ClosedSelectorException, Unit] =
    IO.effect(selector.selectedKeys().remove(key.selectionKey))
      .unit
      .refineToOrDie[ClosedSelectorException]

  val select: IO[Exception, Int] =
    IO.effect(selector.select()).refineToOrDie[IOException]

  val close: IO[IOException, Unit] =
    IO.effect(selector.close()).refineToOrDie[IOException].unit
}

object Selector {

  final val make: IO[IOException, Selector] =
    IO.effect(new Selector(JSelector.open())).refineToOrDie[IOException]
}