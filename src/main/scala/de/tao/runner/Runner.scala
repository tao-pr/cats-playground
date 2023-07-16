package de.tao.runner

import de.tao.config._
import de.tao.common.Screen

import cats.Monad
import cats.Applicative
import cats.MonadThrow
import cats.effect.IO
import cats.effect.std.Console
import cats.effect.kernel.{Sync, Async, MonadCancelThrow}

import scala.Console.{RED, RESET}
import cats.data.EitherT
import java.io.InvalidClassException

trait Runner[F[_], K] {
  val runParams: Option[_ <: RunParams]
  def run: F[K] // effectful, takes no additional input
}

// object Runner {
//   def make[F[_]: Async](
//     cfg: AppConfig,
//     console: Console[F]
//   ): EitherT[F, Throwable, Runner[F, _]] = {
//     cfg.runMode match { // taotodo wrap folowing into F[]
//       case "process-csv" =>
//         val runParams: Option[ProcessCSV] = cfg.runParams
//           .collect{ case p: ProcessCSV => p }
//           .headOption
//         // taotodo wrap following in for {} yield
//           // maybe move to Main instead of make here?
//         EitherT.liftF(ProcessCsvRunner.make[F, Iterable[String]](runParams, console))

//       case "pimc" =>
//         // taotodo extract run params
//         EitherT.liftF(PiMC.make[F](console))

//       case _: String => 
//         // taotodo may need to return EitherT[F, ??]
//         Screen.red(s"Unknown run mode: ${cfg.runMode}")
//         EitherT.leftT(new InvalidClassException(s"Unknown runner: ${cfg.runMode}"))
//     }
//   }
// }
