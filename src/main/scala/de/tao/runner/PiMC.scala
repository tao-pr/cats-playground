package de.tao.runner

import de.tao.config._

import cats.effect.std.Console

/**
  * Estimate Pi with MCMC
  */
class PiMC[F[_]] extends Runner[F, Double]{

  override val runParams: Option[_ <: RunParams] = ???

  override def run: F[Double] = ???
  
}

object PiMC {
  def make[F[_]](console: Console[F]): PiMC[F] = {
    ??? // taotodo
  }
}
