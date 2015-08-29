package classifier

import scalaz._
import scalaz.std.AllInstances._
import scalaz.syntax.all._
import scalaz.stream.Process

object validation {

  trait Validate
  trait Train
  trait Test
  val Validate = Tag.of[Validate]
  val Train = Tag.of[Train]
  val Test = Tag.of[Test]

  def testAccuracy[F[_]:Functor:Foldable:Zip:Unzip,M,E,L:Equal]
                  (train: F[(E,L)] @@ Train, test: F[(E,L)] @@ Test)
                  (implicit c: Classifier[M,E,L]): Double = {

    val m = c.train(Train.unwrap(train))
    val (unlabeled, trueLabel): (F[E],F[L]) = Test.unwrap(test).unfzip
    val approxLabel = c.predict(m, unlabeled)
    val compare: F[(L,L)] = trueLabel.fzip(approxLabel)
    val (correct, total) = compare.foldl((0,0)) {
      case (correct, total) ⇒ {
        case (t, a) =>
          if (t === a) (correct + 1, total + 1) else (correct, total + 1)
      }
    }
    correct.toDouble / total
  }

  def validate[F[_]:Functor:Foldable:Zip:Unzip,HP,M,E,L:Equal]
              (hyperParameters: F[HP],
               instances: F[(E,L)] @@ Train,
               validationSet: F[(E,L)] @@ Validate,
               build: HP ⇒ Classifier[M,E,L]): Option[(HP, Double)] = {

    def i[T](implicit t: T): T = t
    hyperParameters.map[(HP, Double)] { hp ⇒
      hp → testAccuracy[F,M,E,L](instances, Test(Validate.unwrap(validationSet)))(i,i,i,i,i,build(hp))
    }.maximumBy[Double](_._2)
  }

//  def validate[F[_],HP,M,E,L](validationCount: Int,
//                              hyperparameters: Process[F,HP],
//                              instances: Process[F,(E,L)],
//                              build: HP ⇒ Classifier[M,E,L]): Process[F, (HP, Double)] =
//
//    hyperparameters.maximumBy { hp ⇒
//      testAccuracy(instances.drop(validationCount), instances.take(validationCount))(build(hp))
//
//    }


}

trait Take[F[_]] {
  def take[A](fa: F[A], n: Int): F[A]
  def drop[A](fa: F[A], n: Int): F[A]
  def splitAt[A](fa: F[A], n: Int): (F[A], F[A])
}
object Take {

  implicit class syntax[F[_],A](fa: F[A])(implicit t: Take[F]) {
    def take(n: Int) = t.take(fa, n)
    def drop(n: Int) = t.drop(fa, n)
    def splitAt(n: Int) = t.splitAt(fa, n)
  }

  implicit val ilist: Take[IList] = new Take[IList] {
    def take[A](fa: IList[A], n: Int): IList[A] = fa.take(n)
    def drop[A](fa: IList[A], n: Int): IList[A] = fa.drop(n)
    def splitAt[A](fa: IList[A], n: Int): (IList[A], IList[A]) = fa.splitAt(n)
  }

  implicit val list: Take[List] = new Take[List] {
    def take[A](fa: List[A], n: Int): List[A] = fa.take(n)
    def drop[A](fa: List[A], n: Int): List[A] = fa.take(n)
    def splitAt[A](fa: List[A], n: Int): (List[A], List[A]) = fa.splitAt(n)
  }
  }
