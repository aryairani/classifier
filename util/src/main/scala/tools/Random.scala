package tools

import scalaz._, Scalaz._

/** A (probably temporary) set of helper functions for random
  * number generators.
  *
  * Nothing fancy, this is just needed for a very basic simulation
  * of noise. All code was taken shamelessly from Functional Programming
  * in Scala: http://manning.com/bjarnason/
  */
object Random {
  type RNG[A] = State[Long,A]

  /** Implementation of a linear congruential generator */
  val lcg: RNG[Int] = State[Long,Int] { s ⇒
    val s2 = (s * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1)

    (s2, (s2 >>> 16).asInstanceOf[Int])
  }

  /** A random non-negative integer */
  val intPos: RNG[Int] = lcg flatMap {
    case Int.MinValue ⇒ intPos
    case x            ⇒ x.abs.η[RNG]
  }

  /** A random int between 0 (inclusive) and max (exclusive) */
  def intRange(max: Int): RNG[Int] = intPos flatMap { i =>
    // 01234 56789 abcde f
    // e/5 = 2, +1 = 3, *5 = 15, > 16 = false
    // f/5 = 3, +1 = 4, *5 = 20, > 16 = true
    if ((i / max + 1) * max.toLong > Int.MaxValue) intRange(max)
    else (i % max).pure[RNG]
  }

  private final val MaxIntP1 = Int.MaxValue.toDouble + 1D

  /** A random double between 0 (inclusive) and 1 (exclusive) */
  val dbl01: RNG[Double] = intPos map { _ / MaxIntP1 }

  /** true with probability p */
  def bernoulli(p: Double): RNG[Boolean] = dbl01.map(_ < p)

  /** true/false based on a fair flip */
  val bool: RNG[Boolean] = bernoulli(0.5)
}

// todo: separate lib?