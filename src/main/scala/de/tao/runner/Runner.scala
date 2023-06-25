package de.tao.runner

import de.tao.config.AppConfig
import cats.Monad
import cats.effect.IO

trait Runner[F[_], A, K] {
  def run(cfg: AppConfig, input: A): F[K]
}

class NoOpRunner[F[_]: Monad] extends Runner[F, Any, Unit] {
  val F = implicitly[Monad[F]]
  override def run(cfg: AppConfig, input: Any): F[Unit] = {
    IO.println("NoOp runner")
    F.pure({}) // taotodo right way to lift unit?
  }
}

object Runner {
  def make[F[_]: Monad](cfg: AppConfig): F[Runner[F, _, _]] = {
    val F = implicitly[Monad[F]]
    cfg.runMode match {
      case "process-csv" =>
        F.pure(new ProcessCsvRunner[F, Unit, Unit]()) // taotodo take inputs

      case _: String => 
        IO.raiseError(new UnsupportedOperationException(s"Not recognised runner: ${cfg.runMode}"))
        F.pure(new NoOpRunner[F])
    }
  }
}
