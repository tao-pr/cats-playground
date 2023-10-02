package de.tao.runner

import de.tao.config.RaceParams
import de.tao.common.Screen

import cats.effect.std.Console
import cats.effect._
import cats.effect.kernel.Temporal
import cats._
import cats.syntax.all._
import cats.Parallel

import scala.concurrent.duration._
import java.util.concurrent.TimeoutException
import cats.effect.kernel.Outcome.Canceled
import cats.effect.kernel.Outcome.Errored
import cats.effect.kernel.Outcome.Succeeded

sealed abstract class RaceRunner[F[_]: Parallel: Async](implicit
    console: Console[F]
) extends Runner[F, List[Double]] {

  val N: Int
  val failRate: Double
  val timeout: Int

  override def run: F[List[Double]] = {
    for {
      _ <- Screen.green(s"Running race of ${N} threads")

      // generate N threads (fibers)
      threads <- (1 to N).toList.traverse(i =>
        Concurrent[F].start(createTask(i))
      )

      // collect results
      results <- threads.traverse(_.join) // taotodo should join?
      results_ <- results.collect { case Succeeded(d) =>
        d
      }.sequence // List[F] => F[List]
      validResults = results_.flatten

      _ <- Screen.println(s"Completed ${validResults.size} threads") *> Screen
        .printList(validResults, Some(25))

    } yield (validResults)
  }

  def timeoutF(): F[Double] = {
    Temporal[F].sleep(timeout.millis) *> Sync[F].delay(0.0)
  }

  def gen(): F[Double] = {

    val toFail = scala.util.Random.nextDouble() < failRate
    if (toFail) throw new RuntimeException("occassional failure")

    val randomDelay = scala.util.Random.nextInt.abs.toInt % 128
    Temporal[F].sleep(randomDelay.millis) *> Sync[F].delay(
      scala.util.Random.nextDouble()
    )
  }

  /** Each task race with timeout
    */
  def createTask(i: Int): F[Option[Double]] = {
    val leftTask = gen
    val rightTask = timeoutF

    Screen.println(s"Racing task #${i}") *>
      Concurrent[F].racePair(leftTask, rightTask).flatMap {
        case Left((outcome, fiber)) =>
          outcomeToMonad(outcome, i)

        case Right(timeout) =>
          Screen.red(s"Task #${i} timed out after ${timeout} ms") *> Monad[F]
            .pure(None)
      }
  }

  def outcomeToMonad(
      outcome: Outcome[F, Throwable, Double],
      i: Int
  ): F[Option[Double]] = {
    outcome match {
      case Canceled() =>
        Screen.yellow(s"Task #{i} is cancelled") *> Monad[F].pure(None)

      case Errored(e) =>
        Screen.red(s"Task #${i} is errored: $e") *> Monad[F].pure(None)

      case Succeeded(fa) =>
        fa.flatMap { a =>
          Screen.cyan(s"Task #${i} returns ${a}") *> Monad[F].pure(Some(a))
        }
    }
  }

}

object RaceRunner {
  def make[F[_]: Parallel: Async](
      _runParams: Option[RaceParams]
  )(implicit console: Console[F]): RaceRunner[F] = {
    new RaceRunner[F] {

      override val runParams = _runParams
      override val N = runParams.map(_.N).getOrElse(5)
      override val failRate = runParams.map(_.failRate).getOrElse(0.0)
      override val timeout = runParams.map(_.timeout).getOrElse(125)
    }
  }
}
