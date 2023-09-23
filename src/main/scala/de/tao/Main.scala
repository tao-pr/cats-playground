package de.tao

import de.tao.runner._
import de.tao.config._
import de.tao.common.Screen

import cats.implicits._
import cats.effect.IOApp
import cats.effect.IO
import cats.effect.std.Console
import cats.effect.ExitCode
import cats.data.EitherT
import cats.effect.kernel.Sync

case class GenCSV(uuid: String, a: Double, b: Double, c: Double, d: Double)

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val runnerT: EitherT[IO, Throwable, Runner[IO, _]] = EitherT(for {
      cfg <- AppConfig.make[IO]
      console = Console.make[IO]
      _ <- Screen.cyan(s"Running mode: ${cfg.runMode}")(console)
    } yield cfg.runMode match {
      case "generate-csv" =>
        val runParams: Option[GenerateCsv] = cfg.runParams.collect {
          case p: GenerateCsv => p
        }.headOption
        GenerateCsvRunner.make[IO](runParams).asRight

      case "csv-to-json" =>
        import de.tao.common.DataTypes._ // Import implicit JSON encoder
        val runParams: Option[CsvToJson] = cfg.runParams.collect {
          case p: CsvToJson => p
        }.headOption
        CsvToJsonRunner.make[IO, SampleCsv](runParams).asRight

      case "combine-json" =>
        import de.tao.common.DataTypes._ // Import implicit JSON encoder
        val runParams: Option[CombineJson] = cfg.runParams.collect {
          case p: CombineJson => p
        }.headOption
        implicit val M = sumSampleCsvM
        CombineJsonRunner.make[IO, SampleCsv](runParams).asRight

      case "pimc" =>
        implicit val runParams: Option[PiMcmc] = cfg.runParams.collect {
          case p: PiMcmc => p
        }.headOption
        implicit val sync = Sync[IO]
        PiMCRunner.make[IO](runParams).asRight

      case "eval-runner" =>
        implicit val runParams: Option[EvalParams] = cfg.runParams.collect {
          case p: EvalParams => p
        }.headOption
        EvalRunner.make[IO](runParams).asRight

      case _ =>
        new UnsupportedOperationException(
          s"Unknown runner type: ${cfg.runMode}"
        ).asLeft[Runner[IO, _]]
    })

    runnerT.value.flatMap {
      case Left(e) =>
        IO.raiseError(e).map(_ => ExitCode.Error)

      case Right(runner) =>
        runner.run.as(ExitCode.Success)
    }
  }
}
