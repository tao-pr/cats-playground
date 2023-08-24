package de.tao.runner

import de.tao.common.Screen
import de.tao.config.GenerateCsv

import cats.effect.std.Console
import cats.Monad
import cats.Parallel
import cats.syntax.all._ // This makes F[_] for-comprehensible
import cats.effect.kernel.Sync

import java.nio.file.Files
import java.nio.file.Paths

sealed abstract class GenerateCsvRunner[F[_]: Sync : Monad](override val runParams: Option[GenerateCsv])(
  implicit console: Console[F]
) extends Runner[F, Unit] {

  override def run: F[Unit] = {

    // parameters
    val N = runParams.map(_.numFiles).getOrElse(1)
    val nLines = runParams.map(_.numLines).getOrElse(5)
    val outputDir = runParams.map(_.outputDir).getOrElse(".")

    for {
      _ <- Screen.green(s"Generating ${N} CSV files (${nLines} lines per file) into ${outputDir}")
      isCreated <- makeDirExist(outputDir)
      _ <- if (isCreated)
        Screen.println(s"Direction $outputDir created")
        else Monad[F].pure({})
    } yield {}
  }

  def makeDirExist(path: String): F[Boolean] = {
    val existF = Sync[F].delay(Files.exists(Paths.get(path)) && Files.isDirectory(Paths.get(path)))

    Monad[F].ifM(existF)(
      Screen.println(s"Directory $path already exists.") >> Monad[F].pure(false),
      Screen.println(s"Creating $path") >> makeDir(path)
    )
  }

  def makeDir(path: String): F[Boolean] = {
    Sync[F].delay {
      Files.createDirectories(Paths.get(path))
    }.as(true).handleErrorWith{
      throwable =>
        Screen.red(s"FAILED to create $path : ${throwable.getMessage()}") *> Sync[F].pure(false)
    }
  }

  def genFile(nLines: Int): F[String] = {
    ???
  }
  
}

object GenerateCsvRunner {
  def make[F[_]: Sync : Monad](runParams: Option[GenerateCsv])(
    implicit console: Console[F]
  ): GenerateCsvRunner[F] = {
    new GenerateCsvRunner(runParams){}
  }
}
