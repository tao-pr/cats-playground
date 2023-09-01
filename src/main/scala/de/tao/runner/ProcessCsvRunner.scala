package de.tao.runner

import de.tao.common.Screen
import de.tao.config.AppConfig
import de.tao.config.ProcessCSV
import de.tao.common.CsvCodec

import cats.effect.std.Console
import cats.effect.IO
import cats.syntax.all._ // This makes F[_] for-comprehensible
import cats.effect.kernel.Async
import cats.effect.std.Console
import cats.Monad

import scala.jdk.CollectionConverters._
import fs2.io.file.Files
import fs2.io.file.Path
import fs2.{Stream, io, text}
import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import scala.concurrent.ExecutionContext

sealed abstract class ProcessCsvRunner[F[_]: Files : Sync, K](override val runParams: Option[ProcessCSV])(
  implicit console: Console[F], codec: CsvCodec[K]
)
extends Runner[F, Iterable[K]] {

  val F = implicitly[Sync[F]]

  override def run(): F[Iterable[K]] = {
    
    // parameters
    val inputDir = runParams.map(_.inputDir).getOrElse(".")
    val outputDir = runParams.map(_.outputDir).getOrElse(".")

    for {
      _ <- Screen.green(s"Reading csv inputs from dir: ${inputDir}")
      _ <- Screen.green(s"Processed output will be written to dir: ${outputDir}")
      processedFileList <- processDir(inputDir, outputDir)
    } yield processedFileList

    // taotodo ^ add handleError()
  }

  def listAllCsv(inputDir: String): Stream[F, Path] = {
    Files[F].walk(Path(inputDir)).filter(_.toString.endsWith(".csv"))
  }

  def readAndTransfrom(csvPath: Path): Stream[F, Either[Throwable, K]] = {
    Files[F]
      .readAll(csvPath, 4096)
      .through(text.utf8.decode)
      .through(text.lines)
      // .drop(1) if there exists a header
      .map{ line => codec.decode(line) } // string => Either[E, K]
      // taotodo apply some type conversion
  }

  def generateJsonFile(data: Either[Throwable, K]): F[List[K]] = {
    ??? // taotodo
  }

  def processDir(inputDir: String, outputDir: String): F[List[K]] = {
    // Walk the input dir, getting all CSV files 
    for {
      sink <- listAllCsv(inputDir)
        .flatMap(readAndTransfrom)
        .evalMap(generateJsonFile)
        .compile
        .drain
    } yield {
      // how to take List[K] from the stream pipeline?
      ??? // taotodo
    }
    
    
  }
  
  
}

object ProcessCsvRunner {
  def make[F[_]: Files : Sync, K](runParams: Option[ProcessCSV])(
    implicit console: Console[F], codec: CsvCodec[K]): ProcessCsvRunner[F, K] = 
      new ProcessCsvRunner[F, K](runParams){}
}
