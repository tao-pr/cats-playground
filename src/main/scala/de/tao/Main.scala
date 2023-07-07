package de.tao

import cats.effect.IOApp
import cats.effect.IO
import de.tao.config.AppConfig
import de.tao.runner.Runner
import cats.effect.std.Console

object Main extends IOApp.Simple {

  def run: IO[Unit] = { 
    for {
      cfg <- AppConfig.make[IO]
      console = Console.make[IO]
      runner = Runner.make[IO](cfg, console)
    } yield runner.run()
  }
}