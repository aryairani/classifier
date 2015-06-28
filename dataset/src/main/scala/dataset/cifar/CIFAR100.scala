package dataset.cifar

import java.io.InputStream

import tools._

import scalaz.concurrent.Task
import scalaz.stream.{Process, io}

object CIFAR100 {
  case class Labeled(coarse: Byte, fine: Byte, pixels: CifarUnlabeled)

  def loadBatch(is: InputStream): Process[Task, Labeled] =
    Process.constant(3074).toSource.through(io.chunkR(is))
      .map(v => Labeled(v.head, v.tail.head, CifarUnlabeled(v.tail.tail.toArray)))

  import StreamTools.{nonemptyString ⇒ nonEmpty}

  val path = "cifar-100-binary"
  def p(s: String) = s"$path/$s"
  def ps(s: String) =  Thread.currentThread.getContextClassLoader.getResourceAsStream(p(s))

  def trainingStream = loadBatch(ps("train.bin"))
  def testStream = loadBatch(ps("test.bin"))

  def coarseLabelStream = io.linesR(ps("coarse_label_names.txt")).filter(nonEmpty)
  def fineLabelStream = io.linesR(ps("fine_label_names.txt")).filter(nonEmpty)

  lazy val trainingSet = trainingStream.runLog.run
  lazy val testSet = testStream.runLog.run
  lazy val coarseLabels = coarseLabelStream.runLog.run
}
