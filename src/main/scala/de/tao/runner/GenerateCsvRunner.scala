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
import scala.util.Random
import java.nio.file.Path

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
      isCreated <- makeDirExist(outputDir) // taotodo write with ifM
      _ <- if (!isCreated)
          Monad[F].pure({})
        else 
          Screen.println(s"Direction $outputDir ready") *>
          genFiles(N, nLines, outputDir)
    } yield {}
  }

  /**
    * returns true if directy is created or already exists
    */
  def makeDirExist(path: String): F[Boolean] = {
    val existF = Sync[F].delay(Files.exists(Paths.get(path)) && Files.isDirectory(Paths.get(path)))

    Monad[F].ifM(existF)(
      Screen.println(s"Directory $path already exists.") >> Monad[F].pure(true),
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

  def genFiles(N: Int, nLines: Int, dirPath: String): F[List[String]] = {
    (1 to N).toList.traverse{ i => genFile(i, nLines, genFileName(dirPath, i))}
  }

  def genFileName(dirPath: String, i: Int): Path = {
    val chars = "0123456789abcdef"
    val genChar = (_:Any) => chars(Random.nextInt(chars.length))
    val base = Paths.get(dirPath)
    val filenameLen = 5
    
    Paths.get((1 to filenameLen).map(genChar).mkString("file-","",".csv"))
  }

  def genFile(i: Int, nLines: Int, path: Path): F[String] = {
    // taotodo
    Screen.println(s"... Generating file#${i} => ${path.getFileName}") >>
    Monad[F].pure(path.toAbsolutePath().toString())
  }
  
}

object GenerateCsvRunner {
  def make[F[_]: Sync : Monad](runParams: Option[GenerateCsv])(
    implicit console: Console[F]
  ): GenerateCsvRunner[F] = {
    new GenerateCsvRunner(runParams){}
  }
}
