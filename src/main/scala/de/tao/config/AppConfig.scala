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
case class GenerateCsv(
  outputDir: String, 
  numFiles: Int, 
  numLines: Int,
  probMakeMalform: Double)
  extends RunParams
case class CsvToJson(inputDir: String, outputDir: String, parallel: Option[Boolean]) extends RunParams
case class CombineJson(inputDir: String, readTimeout: Int) extends RunParams
case class PiMcmc(iter: Int) extends RunParams

case class AppConfig(
  runMode: String,
  runParams: List[_ <: RunParams],
  verbose: Option[Boolean]
)

// Using pureconfig with cats-effect
// https://github.com/pureconfig/pureconfig/tree/master/modules/cats-effect
object AppConfig {
  def make[F[_]](implicit F: Sync[F]): F[AppConfig] = {
    ConfigSource.default.loadF[F, AppConfig]
  }
}
