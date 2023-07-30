package de.tao.config

import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

import cats.effect.IO
import cats.effect.unsafe.implicits._
import cats.effect.kernel.Sync
import cats.data.{NonEmptyList}

sealed trait RunParams
object NoRunParams extends RunParams
case class ProcessCSV(inputDir: String, outputDir: String, parallel: Option[Boolean]) extends RunParams
case class PiMC(iter: Int) extends RunParams

case class AppConfig(
  runMode: String,
  runParams: List[_ <: RunParams],
  verbose: Option[Boolean]
){
  def print: Unit = {
    val B = Console.BLUE
    val E = Console.RESET
    // taotodo use Screen instead
    IO.println(s"${B}AppConfig: ${this}${E}")
  }
}

// Using pureconfig with cats-effect
// https://github.com/pureconfig/pureconfig/tree/master/modules/cats-effect
object AppConfig {
  def make[F[_]](implicit F: Sync[F]): F[AppConfig] = {
    ConfigSource.default.loadF[F, AppConfig]
  }
}
