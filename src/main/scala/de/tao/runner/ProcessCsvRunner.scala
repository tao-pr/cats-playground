package de.tao.runner

import de.tao.config.AppConfig
import de.tao.config.ProcessCSV
import cats.effect.IO
import cats.Monad

class ProcessCsvRunner[F[_]: Monad, A, K] extends Runner[F, Iterable[A], Iterable[K]] {

  val F = implicitly[Monad[F]]

  override def run(cfg: AppConfig, input: Iterable[A]): F[Iterable[K]] = {
    val result = for {
      params <- params(cfg)
      _ = IO.println(s"Running CSV with: ${params}")
    } yield Seq.empty[K]

    F.pure(result match { // taotodo transform Option => List
      case None => Nil
      case Some(ks) => ks
    })
  }

  def params(cfg: AppConfig): Option[ProcessCSV] = {
    cfg.runParams.collect{ case p: ProcessCSV => p }.headOption
  }
  
}
