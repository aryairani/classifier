package classifier

import com.thesamet.spatial.{DimensionalOrdering, Metric}
import org.scalatest.FunSuite

import scalaz.std.list._

sealed trait Parity
object Parity {
  case object Odd extends Parity
  case object Even extends Parity
}; import Parity._

class KDTreeMapClassifierTest extends FunSuite {

  // Needed by KD Tree implementation
  implicit def metricFromNumeric[A](implicit N: Numeric[A]) = new Metric[A,A] {
    override def distance(x: A, y: A): A = N.abs(N.minus(x,y))
    override def planarDistance(dimension: Int)(x: A, y: A): A = distance(x,y)
  }
  implicit val dimensionalOrdering = new DimensionalOrdering[Int] {
    override def dimensions: Int = 1
    override def compareProjection(dimension: Int)(x: Int, y: Int): Int = x-y
  }
  
  val data = List[(Int,Parity)](1 -> Odd, 12 -> Even, 6 -> Even, 43 -> Odd)

  implicit val intParityClassifier = KNN.kdTree1nnClassifier[Int,Int,Parity]

  test("Classifier quick test") {

    val tree = intParityClassifier.train(data)

    assert(intParityClassifier.predict1(tree, 7) == Even)
    assert(intParityClassifier.predict(tree, List(3,4,5)) == List(Odd,Even,Even))
  }

  test("Classifier syntax quick test") {
    import Classifier.syntax._

    val tree = data.train

    assert(tree.predict1(7) == Even)
    assert(tree.predict(List(3,4,5)) == List(Odd,Even,Even))
  }

  test("classifier contramap test") {
    import Classifier.syntax._

    val tree = data.train

    implicit val stringParityClassifier = intParityClassifier.contramap[String](_.length)

    assert( tree.predict1("hello") == Even )

    val tree2 = List[(String,Parity)]("ok" -> Even, "hello" -> Odd).train
    assert( tree2.predict1("foo") == Even )
  }
}
