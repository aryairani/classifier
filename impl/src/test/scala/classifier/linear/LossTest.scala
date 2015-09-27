package classifier.linear

import breeze.linalg._, org.scalacheck.{Gen, Properties}, org.scalacheck.Prop.forAll

class LossTest extends Properties("Loss") {
  case class MatchingXyW(X: DenseMatrix[Double], y: Int, W: DenseMatrix[Double]) {
//    println(s"X is supposed to represent ${X.cols} instances with ${X.rows} features")
//    println(s"W is supposed to represent ${W.cols} feature weights for instances with ${W.rows} classes")
//    println(s"y is the true class, 0 <= $y < ${W.rows}")
    assert(X.rows == W.cols)
    assert(y < W.rows)
    val numInstances = X.cols
  }
  val matrices: Gen[MatchingXyW] = for {
    classes ← Gen.choose(1,100)
    features ← Gen.choose(1,100)
    instances ← Gen.choose(1,100)
    trueClass ← Gen.choose(1,classes-1)
//    _ = println(s"classes: $classes, features: $features, instances: $instances, trueClass: $trueClass")
  } yield MatchingXyW(DenseMatrix.rand(features,instances), trueClass, DenseMatrix.rand(classes, features))

  property("vector-wise loss = matrix loss") = forAll(matrices) { (p: MatchingXyW) ⇒
    val epsilon = 1e-8
    val vectorized = loss(p.X, p.y, p.W)
    val semiVectorized = (0 until p.numInstances map (i ⇒ loss(p.X(::,i), p.y, p.W))).sum
    (vectorized - semiVectorized).abs < epsilon
  }
}
