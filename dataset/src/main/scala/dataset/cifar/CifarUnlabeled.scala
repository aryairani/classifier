package dataset.cifar

import com.thesamet.spatial.{DimensionalOrdering, Metric}

case class CifarUnlabeled(pixels: Array[Byte])

object CifarUnlabeled {
  def byteDistance(a: Byte, b: Byte): Int = Math.abs((a & 0xFF) - (b & 0xFF))

  implicit val cifarL1Metric = new Metric[CifarUnlabeled,Int] {
    override def distance(x: CifarUnlabeled, y: CifarUnlabeled): Int = {
      assert(x.pixels.length == y.pixels.length)
      x.pixels.zip(y.pixels).map{ case (a,b) => byteDistance(a,b) }.sum
    }

    override def planarDistance(dimension: Int)(x: CifarUnlabeled, y: CifarUnlabeled): Int =
      byteDistance(x.pixels(dimension), y.pixels(dimension))
  }

  implicit val cifarL1DimensionalOrdering = new DimensionalOrdering[CifarUnlabeled] {
    override def dimensions: Int = 3072

    override def compareProjection(dimension: Int)(x: CifarUnlabeled, y: CifarUnlabeled): Int =
      x.pixels(dimension).toInt - y.pixels(dimension).toInt
  }
}
