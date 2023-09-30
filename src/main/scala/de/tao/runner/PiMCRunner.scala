package de.tao.runner

import de.tao.config._
import de.tao.common.Screen

import cats.effect.std.Console
import cats.effect._
import cats.data.EitherT
import cats.{Applicative, Parallel}
import cats.syntax.all._ // This makes F[_] for-comprehensible

/** Estimate Pi with MCMC
  */
sealed abstract class PiMCRunner[F[_]: Parallel](
    override val runParams: Option[PiMcmc]
)(implicit console: Console[F], F: Sync[F])
    extends Runner[F, Double] {

  override def run: F[Double] = {

    val N = runParams.map(_.iter).getOrElse(10)
    val iters = (1 to N).toList

    for {
      _ <- Screen.green(s"Generating Pi from ${N} iters")
      coords <- iters.parTraverse(i => genXY(i))
      numInside = coords.count(identity)

      // area of circle / area of rect = pi / 4
      // thus,                      pi = 4 * (num dots in circle / num dots in rect)
      pi = 4 * numInside.toDouble / N.toDouble
      _ <- Screen.println(s"Estimated Pi = ${pi}")
    } yield pi
  }

  def genXY(i: Int): F[Boolean] = {
    for {
      _ <- Screen.println(s"genXY [${i}]")
      (x, y) = (Math.random(), Math.random())
    } yield Math.abs(
      x * x + y * y
    ) <= 1 // [?] Is this equivalent to F.delay() ?
  }

}

object PiMCRunner {
  def make[F[_]: Parallel](
      runParams: Option[PiMcmc]
  )(implicit sync: Sync[F], console: Console[F]): PiMCRunner[F] = {
    new PiMCRunner[F](runParams) {}
  }
}
