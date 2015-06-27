package dataset.cifar

import java.io.File

import tools._

import scalaz.concurrent.Task
import scalaz.stream.{Process, io}

object CIFAR100 {
  case class Labeled(coarse: Byte, fine: Byte, pixels: CifarUnlabeled)

  def loadBatch(file: File): Process[Task, Labeled] =
    Process.constant(3074).toSource.through(io.fileChunkR(file.getAbsolutePath))
      .map(v => Labeled(v.head, v.tail.head, CifarUnlabeled(v.tail.tail.toArray)))

  val pathHelp = PathHelp.fromString("/Users/arya/Downloads/cifar-100-binary")
  import pathHelp._
  import StreamTools.{nonemptyString => nonEmpty}

  def trainingStream = loadBatch(p("train.bin"))
  def testStream = loadBatch(p("test.bin"))

  def coarseLabelStream = io.linesR(ps("coarse_label_names.txt")).filter(nonEmpty)
  def fineLabelStream = io.linesR(ps("fine_label_names.txt")).filter(nonEmpty)

  lazy val trainingSet = trainingStream.runLog.run
  lazy val testSet = testStream.runLog.run
  lazy val coarseLabels = coarseLabelStream.runLog.run
}
