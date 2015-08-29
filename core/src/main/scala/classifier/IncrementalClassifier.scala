package classifier

import scalaz.{Foldable, Functor}

trait IncrementalClassifier[M,E,L] extends Classifier[M,E,L] { outer ⇒
  def empty: M

  def trainM[F[_]:Functor:Foldable](m: M, labeledInstances: F[(E,L)]): M

  override def train[F[_] : Functor : Foldable](labeledInstances: F[(E, L)]): M =
    trainM(empty, labeledInstances)
}

object IncrementalClassifier {

  object syntax {
    implicit class incrementalTrainSyntax[M, E, L](m: M)(implicit C: IncrementalClassifier[M, E, L]) {
      def trainM[F[_] : Functor : Foldable](labeledInstances: F[(E, L)]): M =
        C.trainM(m, labeledInstances)
    }
  }
}
