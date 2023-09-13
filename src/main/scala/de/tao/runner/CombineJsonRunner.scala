package de.tao.runner

import de.tao.common.Screen
import de.tao.config.CombineJson
import de.tao.common.JsonCodec

import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.syntax.all._ // This makes F[_] for-comprehensible

import fs2.io.file.Files
import fs2.io.file.Path
import fs2.{Stream, io, text}

sealed abstract class CombineJsonRunner[F[_]: Files: Sync, K](
    override val runParams: Option[CombineJson]
)(implicit console: Console[F], jsonCodec: JsonCodec[K])
    extends Runner[F, Iterable[K]] {

  override def run: F[Iterable[K]] = {

    // parameters
    val inputDir = runParams.map(_.inputDir).getOrElse(".")

    for {
      _ <- Screen.green(s"Loading JSON files from: ${inputDir}")
      jsonList <- streamToFileList(listAllJson(inputDir))
      _ <- Screen.printList(jsonList, max=Some(20))

      // taotodo read each json file in parallel

    } yield {

      // taotodo
      Nil
    }
  }

  def listAllJson(inputDir: String): Stream[F, List[Path]] = {
    Files[F]
      .walk(Path(inputDir))
      .filter(_.extName == ".json")
      .map(List(_))
  }

  /**
   * Stream[F, A] => F[A]
   */
  def streamToFileList(stream: Stream[F, List[Path]]): F[List[Path]] = {
    stream.compile.foldMonoid
  }

}

object CombineJsonRunner {
  def make[F[_]: Console: Files: Sync, K](runParams: Option[CombineJson])(
      implicit jsonCodec: JsonCodec[K]
  ) =
    new CombineJsonRunner[F, K](runParams) {}
}
