package de.tao.runner

import de.tao.config._

trait Runner[F[_], K] {
  val runParams: Option[_ <: RunParams]
  def run: F[K] // effectful, takes no additional input
}
