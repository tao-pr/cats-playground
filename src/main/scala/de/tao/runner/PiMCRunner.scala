package de.tao.runner

import de.tao.config._
import de.tao.common.Screen

import cats.effect.std.Console
import cats.effect.Sync
import cats.data.EitherT
import cats.Applicative
import cats.syntax.all._
import cats.syntax.parallel._
import cats.Parallel

/**
  * Estimate Pi with MCMC
  */
sealed abstract class PiMCRunner[F[_]: Parallel](override val runParams: Option[PiMC])(
  implicit console: Console[F], F: Sync[F])
  extends Runner[F, Double]{

  override def run: F[Double] = {

    val N = runParams.map(_.iter).getOrElse(10)
    val iters = (1 to N).toList

    for {
      _ <- Screen.green(s"Generating Pi from ${N} iters")
      coords <- iters.traverse(i => F.delay(genXY(i)))
    } yield {
      val numInside = coords.count(identity)

      // area of circle / area of rect = pi / 4
      // thus,                      pi = 4 * (num dots in circle / num dots in rect)
      
      4 * numInside.toDouble / N.toDouble
    }
  }

  def genXY(i: Int): Boolean = {
    Screen.red(s"genXY ${i} => _")
    val (x,y) = (Math.random(), Math.random())
    Math.abs(x*x + y*y) <= 1
  }
  
}

object PiMCRunner {
  def make[F[_]: Parallel](
    runParams: Option[PiMC]
  )(implicit sync: Sync[F], console: Console[F]): PiMCRunner[F] = {
    new PiMCRunner[F](runParams){}
  }
}
