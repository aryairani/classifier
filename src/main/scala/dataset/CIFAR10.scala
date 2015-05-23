package dataset

import java.io.File

import scodec.bits.ByteVector

import scalaz.concurrent.Task
import scalaz.stream._

/**
 * Created by arya on 5/23/15.
 */
object CIFAR10 {

  case class ByteLabeled(label: Byte, pixels: ByteVector)

  def loadBatch(file: File): Process[Task, ByteLabeled] =
    Process.constant(3073).toSource.through(io.fileChunkR(file.getAbsolutePath))
      .map(v => ByteLabeled(v.head, v.tail))

  def trainingStream(path: File) =
    Process.emitAll(1 to 5)
      .map(i => new File(path, s"data_batch_$i.bin"))
      .flatMap(loadBatch)

  def testStream(path: File) = loadBatch(new File(path, "test_batch.bin"))

  def metaStream(path: File) = io.linesR(new File(path, "batches.meta.txt").getAbsolutePath)

  val path = new java.io.File("/Users/arya/Downloads/cifar-10-batches-bin")
  lazy val trainingSet = trainingStream(path).runLog.run
  lazy val testSet = testStream(path).runLog.run
  lazy val stringLabels = metaStream(path).take(10).runLog.run

}