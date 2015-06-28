package dataset.cifar

import java.io.InputStream

import classifier.SplitLabeled
import tools.StreamTools

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

  def loadBatch(is: InputStream): Process[Task, Labeled] =
    Process.constant(3073).toSource.through(io.chunkR(is))
      .map(v => Labeled(v.head, CifarUnlabeled(v.tail.toArray)))

  val path = "cifar-10-batches-bin"
  def p(s: String) = s"$path/$s"
  def ps(s: String) =  Thread.currentThread.getContextClassLoader.getResourceAsStream(p(s))
  import StreamTools.{nonemptyString ⇒ nonEmpty}

  def trainingStream =
    Process.emitAll(1 to 5)
      .map(i => ps(s"data_batch_$i.bin"))
      .flatMap(loadBatch)
      .map(Labeled.splitLabel.split)

  def testStream = loadBatch(ps("test_batch.bin")).map(Labeled.splitLabel.split)

  def metaStream = io.linesR(ps("batches.meta.txt")).filter(nonEmpty)

  def dotPerN[A](n: Int): Process1[A,String] = Process.await1[A].chunk(n).map(_ => ".")

  def trainingSet = trainingStream.runLog.run.toIndexedSeq // https://github.com/scalaz/scalaz-stream/issues/392
  def testSet = testStream.runLog.run.toIndexedSeq
  def stringLabels = metaStream.runLog.run.toIndexedSeq

}
