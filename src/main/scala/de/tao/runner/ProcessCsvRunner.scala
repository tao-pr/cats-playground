package de.tao.runner

import de.tao.config.AppConfig
import de.tao.config.ProcessCSV
import cats.effect.IO
import cats.Monad
import cats.effect.kernel.Async
import cats.effect.std.Console

// taotodo: convert to trait?
class ProcessCsvRunner[F[_]: Monad, K](override val runParams: Option[ProcessCSV]) 
extends Runner[F, Iterable[K]] {

  val F = implicitly[Monad[F]]

  override def run(): F[Iterable[K]] = {
    ??? // taotodo
  }
  
}

object ProcessCsvRunner {
  def make[F[_]: Async, K](
    runParams: Option[ProcessCSV],
    console: Console[F]
  ): ProcessCsvRunner[F, K] = {
    // taotodo how to create Async Monad?
    ???
  }
}
