package de.tao.runner

import de.tao.config.AttemptParams
import de.tao.common.Screen

import cats.effect.std.Console
import cats.effect._
import cats.syntax.all._
import cats.data.Ior
import cats.data.Validated
import cats.data.Ior.Both
import cats.MonadThrow
import cats.ApplicativeError
import cats.Applicative
import cats.kernel.Monoid
import cats.Parallel
import cats.data.Validated.Valid

// Types of generated data point (which can be invalid)
case class Point(x: Double, y: Double, z: Double) {
  def ++(that: Point): Point = Point(
    x + that.x,
    y + that.y,
    z + that.z
  )

  def /(scale: Int): Point = Point(
    x / scale.toDouble,
    y / scale.toDouble,
    z / scale.toDouble
  )
}
trait NoValue { val why: String }
case object OutOfBound extends NoValue { override val why = "out of bound" }
case class InvalidE(override val why: String) extends NoValue
case object JustInvalid extends NoValue { override val why = "just invalid" }

object Mapper {
  def toValidated[E, B](
      fe: NoValue => E,
      fv: Point => B
  )(ior: NoValue Ior Point): Validated[E, B] = (ior match {
    case Ior.Left(a)  => fe(a).leftIor
    case Ior.Right(b) => fv(b).rightIor
    case Both(a, b)   => Ior.Both(fe(a), fv(b))
  }).toValidated
}

object Point {
  implicit val monoid: Monoid[Validated[NoValue, Point]] =
    new Monoid[Validated[NoValue, Point]] {
      def empty: Validated[NoValue, Point] = Validated.invalid(JustInvalid)
      def combine(
          x: Validated[NoValue, Point],
          y: Validated[NoValue, Point]
      ): Validated[NoValue, Point] = (x, y) match {
        case (Validated.Invalid(_), a)                => a
        case (a, Validated.Invalid(_))                => a
        case (Validated.Valid(a), Validated.Valid(b)) => Validated.Valid(a ++ b)
      }
    }
}

abstract sealed class AttemptRunner[F[_]: Sync: Parallel](implicit
    console: Console[F]
) extends Runner[F, List[Double]] {

  override val runParams = None

  // Point generator parameters
  val successRate: Double
  val hardFailRate: Double
  val N: Int

  override def run: F[List[Double]] = {
    for {
      _ <- Screen.green(s"Generating ${N} data points")

      // generate data points (may failed or half-failed)
      points <- (1 to N).toList.traverse(_ => genPoint)
      numFailed = points.count(_.isLeft)
      numWarned = points.count(_.isBoth)
      numGood = points.count(_.isRight)
      _ <- Screen.println(s"... ${numGood} good data points")
      _ <- Screen.println(s"... ${numWarned} half-good data points")
      _ <- Screen.red(s"... ${numFailed} failed data points")

      // validate data points
      _ <- Screen.green("Validating data points")
      validated = points.map(Mapper.toValidated(identity, identity))
      numValid = validated.count(_.isValid)
      _ <- Screen.println(s"... ${numValid} valid data points")

      // concurrently combine all valid points (Monoidic)
      combined <- validated.parTraverse(v => Sync[F].delay(v)).map(_.combineAll)
      _ <- combined match {
        case Validated.Invalid(e) => Screen.red(s"Failed to combine: $e")
        case Valid(a)             => Screen.cyan(s"Mean point: ${a / numValid}")
      }

    } yield (Nil)
  }

  def genDouble: Double = scala.util.Random.nextDouble()

  /** Generate 3D point which can fail or with warning
    */
  def genPoint(): F[NoValue Ior Point] = Sync[F].delay {
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
  def make[F[_]: Sync: Parallel](
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
