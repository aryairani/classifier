package classifier

import com.thesamet.spatial.KDTreeMap
import dataset.cifar.{CifarUnlabeled, CIFAR10}
import scalaz.Scalaz._
import scalaz.std.indexedSeq.indexedSeqInstance

object Cifar10Demo {

  def main(args: Array[String]): Unit = {
    import CIFAR10._
    import Classifier.syntax._
    import KNN.kdTree1nnClassifier

    implicit val classifier: Classifier.KE[KDTreeMap,CifarUnlabeled,Byte] = kdTree1nnClassifier[CifarUnlabeled,Int,Byte]

    val tree = trainingSet.train

    //  def test[M,E,L](m: M)(testSet: )(implicit C: Classifier[M,E,L]): Double

    val correct = testSet.zipWithIndex foldMap { case ((instance, label),index) =>
      println(index)
      if (tree.predict1(instance) == label) 1 else 0
    }
    val accuracy = correct.toDouble / testSet.length
    println(s"accuracy: $accuracy")
  }

}
