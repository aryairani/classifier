package dataset.cifar

import java.io.File

import classifier.SplitLabeled
import tools.{PathHelp, StreamTools}

import scalaz.concurrent.Task
import scalaz.stream._

object CIFAR10 {

  case class Labeled(label: Byte, pixels: CifarUnlabeled)
  object Labeled {
    implicit val splitLabel = new SplitLabeled[Labeled,Byte,CifarUnlabeled] {
      override def label(li: Labeled): Byte = li.label
      override def split(li: Labeled): (CifarUnlabeled, Byte) = (li.pixels, li.label)
      override def unlabeled(li: Labeled): CifarUnlabeled = li.pixels
    }
  }

  def loadBatch(file: File): Process[Task, Labeled] =
    Process.constant(3073).toSource.through(io.fileChunkR(file.getAbsolutePath))
      .map(v => Labeled(v.head, CifarUnlabeled(v.tail.toArray)))

  val pathHelp = PathHelp.fromString("/Users/arya/Downloads/cifar-10-batches-bin")
  import pathHelp._
  import StreamTools.{nonemptyString => nonEmpty}

  def trainingStream =
    Process.emitAll(1 to 5)
      .map(i => p(s"data_batch_$i.bin"))
      .flatMap(loadBatch)
      .map(Labeled.splitLabel.split)

  def testStream = loadBatch(p("test_batch.bin")).map(Labeled.splitLabel.split)

  def metaStream = io.linesR(p("batches.meta.txt").getAbsolutePath).filter(nonEmpty)

  def dotPerN[A](n: Int): Process1[A,String] = Process.await1[A].chunk(n).map(_ => ".")

  def trainingSet = trainingStream.runLog.run.toIndexedSeq // https://github.com/scalaz/scalaz-stream/issues/392
  def testSet = testStream.runLog.run.toIndexedSeq
  def stringLabels = metaStream.runLog.run.toIndexedSeq

}
