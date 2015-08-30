package classifier

import scalaz.{Functor, Foldable}
import scalaz.syntax.functor._

/**
 * @tparam M classifier's memory
 * @tparam E instance's external type
 * @tparam L label's type
 */
trait Classifier[M,E,L] { outer =>

  /** populate Memory with labeled E -> L examples */
  def train[F[_]:Functor:Foldable](labeledInstances: F[(E, L)]): M

  /** classify E as L */
  def predict[F[_]:Functor](m: M, data: F[E]): F[L]

  /** classify E as (E,L) */
  def label[F[_]:Functor](m: M, data: F[E]): F[(E,L)]



  /////////

  /** convert E2 to E before training/classifiying */
  def contramap[E2](load: E2 => E): Classifier[M,E2,L] =
    new Classifier[M,E2,L] { inner =>

      override def train[F[_]:Functor:Foldable](labeledInstances: F[(E2, L)]): M =
        outer.train(labeledInstances.map { case (e2, l) => load(e2) -> l })

      override def predict[F[_]:Functor](m: M, unlabeled: F[E2]): F[L] =
        outer.predict(m, unlabeled.map(load))

      override def label[F[_] : Functor](m: M, unlabeled: F[E2]): F[(E2, L)] =
        unlabeled.fproduct(e2 => outer.predict1(m, load(e2)))
    }

  /** populate Memory with labeled E -> L examples */
  def splitTrain[F[_]:Functor:Foldable,LE](labeledInstances: F[LE])(implicit L:SplitLabeled[LE,L,E]): M =
    train(labeledInstances.map(L.split))

  /** classify E as L */
  def predict1(m: M, data: E): L = predict[scalaz.Id.Id](m, data)(scalaz.idInstance)
}

object Classifier {
  object syntax {
    implicit class trainSyntax[F[_]:Functor:Foldable,M,E,L](data: F[(E,L)])(implicit C: Classifier[M,E,L]) {
      /** build M by training on labeled data */
      def train: M = C.train(data)
    }
    implicit class splitTrainSyntax[F[_]:Functor:Foldable,LE,M,E,L](data: F[LE])(implicit val C: Classifier[M,E,L], L: SplitLabeled[LE,L,E]) {
      /** build M by training on labeled data */
      def splitTrain: M = C.splitTrain[F, LE](data)
    }
    implicit class classifySyntax[M,E,L](m: M) {
      import scalaz.Id.Id, scalaz.idInstance
      def predict1(data: E)(implicit C: Classifier[M,E,L]): L = C.predict[Id](m, data)
      def predict[F[_]:Functor](data: F[E])(implicit C: Classifier[M,E,L]): F[L] = C.predict[F](m, data)
    }
  }

  /** Can classify E as L based on I to L experience and the ability to convert E to I */
  type K[M0[_,_],E,I,L] = Classifier[M0[I,L],E,L]
  /** Can classify E as L based on I to L experience and the ability to convert E to I */
  trait KT[M0[_,_],E,I,L] extends Classifier[M0[I,L],E,L] // the T stands for Trait

  /** Can classify E as L based on E to L experience */
  type KE[M[_,_],E,L] = K[M,E,E,L]
  /** Can classify E as L based on E to L experience */
  trait KET[M[_,_],E,L] extends K[M,E,E,L] // the T stands for Trait
}



