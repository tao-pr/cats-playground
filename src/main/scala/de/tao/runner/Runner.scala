package de.tao.runner

import de.tao.config._

import cats.Monad
import cats.effect.IO
import cats.effect.kernel.{Sync, Async, MonadCancelThrow}
import cats.effect.std.{Console}
import cats.Applicative
import cats.MonadThrow

trait Runner[F[_], K] {
  val runParams: Option[_ <: RunParams]
  def run(): F[K]
}

abstract class NoOpRunner[F[_]: Sync] extends Runner[F, Unit] {
  val console: Console[F]
  override val runParams = None

  override def run(): F[Unit] = {
    console.println("Running NoOpRunner")
  }
}

object NoOpRunner {
  def make[F[_]: Sync](console: Console[F]): F[NoOpRunner[F]] = {
    Applicative[F].pure(new NoOpRunner[F]{
      override val console: Console[F] = console
    })
  }
}

object Runner {
  def make[F[_]: Async](cfg: AppConfig, console: Console[F]): F[Runner[F, _]] = {
    cfg.runMode match {
      case "process-csv" =>
        val runParams: Option[ProcessCSV] = cfg.runParams
          .collect{ case p: ProcessCSV => p }
          .headOption
        ProcessCsvRunner.make[F, Iterable[String]](runParams, console)

      case _: String => 
        // taotodo create sync loop
        console.error(s"Unrecognised runner: ${cfg.runMode}")
        NoOpRunner.make[F](console)
    }
  }
}
