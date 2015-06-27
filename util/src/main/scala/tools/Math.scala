package tools

import tools.Random.RNG

import scalaz._, Scalaz._
import scalaz.effect.ST

object Math {
//  def choose[A](k: Int)(from: Set[A]): RNG[Set[A]] = {
//    import State._
//    def loop(k: Int, selected: Set[A], available: Set[A]): RNG[Set[A]] = k match {
//      case 0 => selected.pure[RNG]
//      case k => for {
//        pick <- Random.intRange(available.size)
//      }
//    }
//    loop(k, Set(), from)
//  }

//  def choose[A](n: Int)(from: Seq[A]): Seq[A]
//  def choose[F[_]:Foldable:PlusEmpty,A](n: Int, from: F[A]): F[A]
//  def choose[F[_]:Foldable,G[_]:PlusEmpty,A](n: Int, from: F[A]): G[A] = {
//    def loop(k: Int, selected: G[A], available: F[A])
//  }

//  def chooseKwithReplacement(n: Int, k: Int): RNG[ImmutableArray[Int]] = {
//    def loop(i: Int, selections: Array[Int]): RNG[Array[Int]]
//  }
//  def chooseIndices(n: Int, k: Int): RNG[ImmutableArray[Int]] = {
//    def loop(k: Int, selected: Set[Int], available: Set[Int]): RNG[Set[Int]] = k match {
//      case 0 => selected.pure[RNG]
//      case k => for {
//        pick <= Random.intRange()
//      }
//    }
//  }

  /*
    To shuffle an array a of n elements (indices 0..n-1):
        for i from n − 1 downto 1 do
            j ← random integer such that 0 ≤ j ≤ i
            exchange a[j] and a[i]
  */
  // StateT[STA,Long,_] = Long => STA[(S,_)]
//  def fisherYates[A](a: Array[A]): RNG[ImmutableArray[A]] = {
//    if (a.isEmpty) ImmutableArray.fromArray(a).pure
//    else {
//      import effect._
//      import ST._
//
//      def foo[S](arr: STArray[S,A]) = {
//        val size = arr.size
//        for {
//          swaps <- (size-1 to 1 by -1).toList map (i => for {
//            j <- Random.intRange(i+1)
//            x <- arr.read(i)
//            y <- arr.read(j)
//            _ <- arr.write(j,x)
//            _ <- arr.write(i,y)
//          })
//        }
//      }
//
////      type STA[S] = ST[S, ImmutableArray[A]]
////      ST.runST(new Forall[STA] {
////        def apply[S] = for {
////          arr <- newArr[S,A]
////
////        }
////      })
//    }
//  }

  def fisherYates[A](a: Array[A]): RNG[ImmutableArray[A]] = {
    if (a.isEmpty) ImmutableArray.fromArray(a).pure[RNG]
    else {
      State[Long,ImmutableArray[A]] { s0 ⇒
        var seed = s0
        def randInt(max: Int) = Random.intRange(max)(seed) match {
          case (newSeed, r) ⇒ seed = newSeed; r
        }
        val wc = a.clone()
        val size = wc.size
        def swap(i: Int, j: Int) = {
          val temp = wc(i)
          wc(i) = wc(j)
          wc(j) = temp
        }

        // do the swapping
        size-1 to 1 by -1 foreach {i ⇒ swap(i, randInt(i+1)) }

        val result = ImmutableArray.fromArray[A](wc)
        seed → result
      }
    }
  }

}
