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

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = { 
    val runnerT: EitherT[IO, Throwable, Runner[IO, _]] = EitherT(for {
      cfg <- AppConfig.make[IO]
      console = Console.make[IO]
      _ <- Screen.cyan(s"Running mode: ${cfg.runMode}")(console)
    } yield cfg.runMode match {
      case "process-csv" =>
        val runParams: Option[ProcessCSV] = cfg.runParams
          .collect{ case p: ProcessCSV => p }
          .headOption
        ProcessCsvRunner.make[IO, Iterable[String]](runParams, console).asRight

      case "pimc" =>
        implicit val runParams: Option[PiMC] = cfg.runParams
          .collect{ case p: PiMC => p }
          .headOption
        implicit val sync = Sync[IO] // taotodo is this the right way to instantiate?
        PiMCRunner.make[IO](runParams).asRight

      case _ =>
        new UnsupportedOperationException(s"Unknown runner type: ${cfg.runMode}").asLeft[Runner[IO, _]]
    })

    runnerT.value.flatMap{
      case Left(e) => 
        IO.raiseError(e).map(_ => ExitCode.Error)

      case Right(runner) => 
        runner.run.as(ExitCode.Success)
    }
  }
}