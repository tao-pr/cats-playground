package de.tao.runner

import de.tao.common.Screen
import de.tao.config.CombineJson
import de.tao.common.JsonCodec

import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.syntax.all._ // This makes F[_] for-comprehensible
import cats.Parallel
import cats.Monad

import fs2.io.file.{Files, Flags}
import fs2.io.file.Path
import fs2.{Stream, io, text}
import javax.swing.text.html.BlockView
import cats.kernel.Monoid

sealed abstract class CombineJsonRunner[F[_]: Files: Sync: Parallel, K](
    override val runParams: Option[CombineJson]
)(implicit console: Console[F], jsonCodec: JsonCodec[K], M: Monoid[K])
    extends Runner[F, K] {

  override def run: F[K] = {

    // parameters
    val inputDir = runParams.map(_.inputDir).getOrElse(".")

    for {
      _ <- Screen.green(s"Loading JSON files from: ${inputDir}")
      pathList <- streamToFileList(listAllJson(inputDir))
      _ <- Screen.printList(pathList, max = Some(20))

      // read json files in parallel
      jsons <- pathList.parTraverse { pathToEitherJson }
      countFailed = jsons.collect { case Left(e) => e }.size
      _ <-
        if (countFailed > 0) // report how many failed
          Screen.red(s"Failed to read ${countFailed} files")
        else Screen.println(s"Successfully read all ${jsons.length} files")

      // only take valid jsons file combination
      validJsons <- jsons.collect { case Right(json) => json }.pure[F]
      _ <-
        if (validJsons.isEmpty)
          Screen.println(s"No valid JSON files to combine")
        else Monad[F].unit

      // Combine all JSONs
      combined = M.combineAll(validJsons)
      _ <- Screen.println(s"combined JSON : ${combined}")
    } yield { combined }
  }

  def listAllJson(inputDir: String): Stream[F, List[Path]] = {
    Files[F]
      .walk(Path(inputDir))
      .filter(_.extName == ".json")
      .map(List(_))
  }

  /** Stream[F, A] => F[A]
    */
  def streamToFileList(stream: Stream[F, List[Path]]): F[List[Path]] = {
    stream.compile.foldMonoid
  }

  def pathToEitherJson(path: Path): F[Either[Throwable, K]] = {
    Files[F]
      .readAll(path, 4096, Flags.Read)
      .through(text.utf8.decode)
      .compile
      .foldMonoid // combine all bytes
      .map { jsonCodec.decode }
  }
}

object CombineJsonRunner {
  def make[F[_]: Console: Files: Sync: Parallel, K](
      runParams: Option[CombineJson]
  )(implicit
      jsonCodec: JsonCodec[K],
      M: Monoid[K]
  ) =
    new CombineJsonRunner[F, K](runParams) {}
}
