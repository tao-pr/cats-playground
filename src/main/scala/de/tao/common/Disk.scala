package de.tao.common

import cats.Monad
import cats.effect.kernel.Sync
import cats.effect.std
import cats.syntax.all._ // This makes F[_] for-comprehensible
import cats.effect.std.Console

import java.nio.file.Files
import java.nio.file.Paths

object Disk {

  /** returns true if directly is created or already exists
    */
  def makeDirExist[F[_]: Sync: Console](path: String): F[Boolean] = {
    val existF = Sync[F].delay(
      Files.exists(Paths.get(path)) && Files.isDirectory(Paths.get(path))
    )

    Monad[F].ifM(existF)(
      Screen.println(s"Directory $path already exists.") >> Monad[F].pure(true),
      Screen.println(s"Creating $path") >> makeDir(path)
    )
  }

  def makeDir[F[_]: Sync: Console](path: String): F[Boolean] = {
    Sync[F]
      .delay {
        Files.createDirectories(Paths.get(path))
      }
      .as(true)
      .handleErrorWith { throwable =>
        Screen.red(
          s"FAILED to create $path : ${throwable.getMessage()}"
        ) *> Sync[F].pure(false)
      }
  }

}
