package classifier

import breeze.linalg._

import scalaz.{Tag, @@}
import scalaz.syntax.tag._

package object linear {
  sealed trait Label; val Label = Tag.of[Label]
  sealed trait Feature; val Feature = Tag.of[Feature]
  sealed trait Instance; val Instance = Tag.of[Instance]
  sealed trait Loss; val Loss = Tag.of[Loss]

  trait Vs[Row,Col]; def Vs[Row,Col] = Tag.of[Row Vs Col]

  def loss(x: Vector[Double], y: Int, W: Matrix[Double]): Double = {
    val ∆ = 1.0
    val scores: Vector[Double] = W * x
    val margins: Vector[Double] = max(scores - scores(y) + ∆, 0.0)
    margins(y to y) := 0.0
    sum(margins)
  }

  /**
   * @param X 3073 rows x 50,000 columns
   * @param y label
   * @param W 10 rows x 3073 columns
   * @return
   */
  def loss(X: DenseMatrix[Double], y: Int, W: DenseMatrix[Double]): Double = {
    def numInstances = X.cols
    def numFeatures = X.rows
    def numLabels = W.rows

    val ∆ = 1.0
    val scores: DenseMatrix[Double] = W * X // 10 rows x 50,000 columns
    val yScore: Transpose[DenseVector[Double]] = scores(y,::) // 1 row x 50,000 columns
    val margins: DenseMatrix[Double] = max((scores.t(::,*) - yScore.t).t + ∆, 0.0) // 10 x 50k
    margins(y,::) := 0.0
    val loss: Double = sum(margins)
    loss
  }

  implicit class labeledMatrixSyntax[R1,C1](A: DenseMatrix[Double] @@ (R1 Vs C1)) {
    def *[C2](B: DenseMatrix[Double] @@ (C1 Vs C2)): DenseMatrix[Double] @@ (R1 Vs C2) =
      Vs[R1,C2](A.unwrap * B.unwrap)
  }

  def loss(X: DenseMatrix[Double] @@ (Feature Vs Instance),
           y: Int @@ Label,
           W: DenseMatrix[Double] @@ (Label Vs Feature)): Double @@ Loss =
    Loss(loss(Vs[Feature,Instance].unwrap(X), Label.unwrap(y), Vs[Label,Feature].unwrap(W)))


}
