package de.tao.runner

import de.tao.common.Screen
import de.tao.config.AppConfig
import de.tao.config.CsvToJson
import de.tao.common.{CsvCodec, JsonCodec}
import de.tao.common.Disk._

import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._

import cats.effect.std.Console
import cats.effect.IO
import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import cats.effect.kernel.Async
import cats.syntax.all._ // This makes F[_] for-comprehensible
import cats.Monad

import fs2.io.file.{Files, Flags}
import fs2.io.file.Path
import fs2.{Stream, io, text}

import java.nio.file.{Files => nioFiles}

sealed abstract class CsvToJsonRunner[F[_]: Files: Sync, K](
    override val runParams: Option[CsvToJson]
)(implicit
    console: Console[F],
    csvCodec: CsvCodec[K],
    jsonCodec: JsonCodec[K]
) extends Runner[F, Unit] {

  val F = implicitly[Sync[F]]

  override def run: F[Unit] = {

    // parameters
    val inputDir = runParams.map(_.inputDir).getOrElse(".")
    val outputDir = runParams.map(_.outputDir).getOrElse(".")

    for {
      _ <- Screen.green(s"Reading csv inputs from dir: ${inputDir}")
      _ <- Screen.green(
        s"Processed output will be written to dir: ${outputDir}"
      )
      isCreated <- makeDirExist(outputDir)
      _ <-
        if (!isCreated) // This seems to be clearer than Monad[F].ifM
          Monad[F].unit
        else
          Screen.println(s"Direction $outputDir ready") *>
            processDir(inputDir, outputDir)
    } yield {}
  }

  def listAllCsv(inputDir: String): Stream[F, Path] = {
    Files[F].walk(Path(inputDir)).filter(_.extName == ".csv")
  }

  def readAndTransfrom(csvPath: Path): Stream[F, Either[Throwable, K]] = {
    Files[F]
      .readAll(csvPath, 4096, Flags.Read)
      .through(text.utf8.decode)
      .through(text.lines)
      // .drop(1) if there exists a header
      .map { line =>
        csvCodec.decode(line) // either
      }
  }

  /** Process each data point and write into JSON format
    */
  def generateJsonFile(
      outputDir: String
  )(data: Either[Throwable, K]): F[Unit] = {

    toOptional(data).flatMap { opt =>
      opt
        .map { p =>
          jsonCodec.encode(p) match {
            case Left(e)     => throw e
            case Right(json) =>
              // Write json to file
              val filePath = Path(outputDir) / (json.hashCode + ".json")
              for {
                _ <- Screen.println(s"Writing json file: ${filePath}")
                _ <- writeJsonFile(filePath, json)
              } yield {}
          }
        }
        .getOrElse(Monad[F].unit)
    }
  }

  def writeJsonFile(filePath: Path, json: String): F[_] = {
    Sync[F].delay(nioFiles.write(filePath.toNioPath, json.getBytes))
  }

  def toOptional(data: Either[Throwable, K]): F[Option[K]] = {

    data match {
      case Left(throwable) =>
        Screen.red(s"REJECTED data from: $throwable") *> Monad[F].pure(None)

      case Right(value) =>
        Screen.println(s"Generating output json: ${value}") *>
          Monad[F].pure(Some(value))
    }
  }

  def processDir(inputDir: String, outputDir: String): F[Unit] = {
    // Walk the input dir, getting all CSV files
    for {
      sink <- listAllCsv(inputDir)
        .flatMap(readAndTransfrom)
        .evalMap(generateJsonFile(outputDir))
        .compile
        .drain
    } yield {}
  }
}

object CsvToJsonRunner {
  def make[F[_]: Console: Files: Sync, K](runParams: Option[CsvToJson])(implicit
      csvCodec: CsvCodec[K],
      jsonCodec: JsonCodec[K]
  ): CsvToJsonRunner[F, K] =
    new CsvToJsonRunner[F, K](runParams) {}
}
