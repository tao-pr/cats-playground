package de.tao

import cats.effect.IOApp
import cats.effect.IO
import de.tao.config.AppConfig
import de.tao.runner.Runner

object Main extends IOApp.Simple {

  // This is your new "main"!
  def run: IO[Unit] = for {
    cfg <- AppConfig.make[IO]
    _ = cfg.print
    runner <- Runner.make[IO](cfg)
  } yield()

  // taotodo run async
}