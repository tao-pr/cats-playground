package de.tao.runner

import de.tao.config._
import de.tao.common.Screen

import cats.Monad
import cats.Applicative
import cats.MonadThrow
import cats.effect.IO
import cats.effect.std.Console
import cats.effect.kernel.{Sync, Async, MonadCancelThrow}

import scala.Console.{RED, RESET}
import cats.data.EitherT
import java.io.InvalidClassException

trait Runner[F[_], K] {
  val runParams: Option[_ <: RunParams]
  def run: F[K] // effectful, takes no additional input
}
