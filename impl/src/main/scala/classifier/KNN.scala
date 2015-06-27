package classifier

import com.thesamet.spatial.{KDTreeMap, Metric, DimensionalOrdering}

import scalaz.{Foldable, Functor}
import scalaz.syntax.foldable._
import scalaz.syntax.functor._

/**
 * Created by arya on 5/23/15.
 */
object KNN {

  def kdTree1nnClassifier[A:DimensionalOrdering,R:Numeric,L](implicit m: Metric[A,R]): Classifier.KE[KDTreeMap,A,L] =
    new Classifier.KET[KDTreeMap,A,L] {
      override def train[F[_] : Functor : Foldable](data: F[(A, L)]): KDTreeMap[A, L] =
        KDTreeMap(data.toIndexedSeq: _*)

      override def predict[F[_] : Functor](tree: KDTreeMap[A, L], instances: F[A]): F[L] =
        instances.map(tree.findNearest(_,1).head._2)

      override def label[F[_] : Functor](tree: KDTreeMap[A, L], instances: F[A]): F[(A, L)] =
        instances.map(tree.findNearest(_,1).head)
    }

//  def kdTreeKnnClassifier[A:DimensionalOrdering,R:Numeric,L](k: Int)(implicit m: Metric[A,R]): Classifier.KE[KDTreeMap,A,L] = new Classifier.KET[KDTreeMap,A,L] {
//    override def train[F[_] : Functor : Foldable](data: F[(A, L)]): KDTreeMap[A, L] =
//      KDTreeMap(data.toIndexedSeq: _*)
//
//    override def predict[F[_] : Functor](tree: KDTreeMap[A, L], instances: F[A]): F[L] =
//      instances.map(tree.findNearest(_,k).???._2) // how to choose?
//
//    override def label[F[_] : Functor](tree: KDTreeMap[A, L], instances: F[A]): F[(A, L)] =
//      instances.map(tree.findNearest(_,k).???) // how to choose?
//  }

  /*
  What is a classifier?

   train: F[(Instance,Label)] => M
   classify: M => F[Instance] => Label

   */
  type train[F[_],M,E,L] = F[(E, L)] => M
  type classify[F[_],M,E,L] = M => F[E] => F[L]

  type train0[F[_],M[_,_],E,I,L] = F[(E, L)] => M[I,L]
  type classify0[F[_],M[_,_],E,I,L] = M[I,L] => F[E] => F[L]
}

// todo: k-fold validation
// todo: hyperparameter search: (k)NN, various metrics
// todo: bindings for FLANN? - http://www.cs.ubc.ca/research/flann/

/*
  clasification section summary:

    - defined image classification
    - introduce NN classifier
    - validation sets
    - cross-validation
    - testing accuracy after hyperparameter search
    - NN gets ~40% on CIFAR-10
    - L1/L2 distances on raw pixel values is lousy because distances correlate more strongly with backgrounds/colors than with semantic content

 */
