package de.tao.runner

import de.tao.config.AttemptParams

import cats.effect.std.Console
import cats.effect._
import cats.syntax.all._
import cats.data.Ior
import cats.data.Validated
import cats.data.Ior.Both

// Types of generated data point (which can be invalid)
case class Point(x: Double, y: Double, z: Double)
trait Invalid { val why: String }
case object OutOfBound extends Invalid { override val why = "out of bound" }
case class InvalidE(override val why: String) extends Invalid

// taotodo: contravariant which maps [Invalid Ior Point] to ???
object Mapper {
  def iorTo[E, B](
      ior: Invalid Ior Point,
      fe: Invalid => E,
      fv: Point => B
  ): Validated[E, B] = (ior match {
    case Ior.Left(a) => fe(a).leftIor
    case Ior.Right(b) => fv(b).rightIor
    case Both(a, b) => Ior.Both(fe(a), fv(b))
  }).toValidated
}

abstract sealed class AttemptRunner[F[_]: Sync](implicit
    console: Console[F]
) extends Runner[F, List[Double]] {

  override val runParams = None

  // Point generator parameters
  val successRate: Double
  val hardFailRate: Double
  val N: Int

  // taotodo:

  // MonadThrow, ApplicativeError, Validated
  // Kleisli -> KleisliT
  // Contravariant -> using F[A].contramap(A -> B) so we can make context F[B]

  override def run: F[List[Double]] = {
    ???
  }

  def genDouble: Double = scala.util.Random.nextDouble()

  /** Generate 3D point which can fail or with warning
    */
  def genPoint: Invalid Ior Point = {
    if (genDouble < successRate) {
      Point(genDouble, genDouble, genDouble).rightIor
    } else if (genDouble < hardFailRate) {
      InvalidE("fail").leftIor
    } else
      // Superposition, point is generated but with warning
      Ior.both(
        OutOfBound,
        Point(genDouble * 10, genDouble * 10, genDouble * 10)
      )
  }
}

object AttemptRunner {
  def make[F[_]: Sync](
      _params: Option[AttemptParams]
  )(implicit console: Console[F]): AttemptRunner[F] =
    new AttemptRunner[F] {
      override val hardFailRate: Double =
        _params.map(_.hardFailRate).getOrElse(0.0)
      override val successRate: Double =
        _params.map(_.successRate).getOrElse(0.35)
      override val N: Int = _params.map(_.n).getOrElse(10)
    }
}