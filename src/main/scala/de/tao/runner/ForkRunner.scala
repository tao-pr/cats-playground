package de.tao.runner

import de.tao.config.ForkParams
import de.tao.common.Screen

import cats.effect.Concurrent
import cats.effect.Ref
import cats.effect.kernel.Temporal
import cats.effect.std.Console
import cats.Parallel
import cats.syntax.all._
import cats._

import scala.concurrent.duration._

/** ForkRunner runs a hierarchy of mixed blocking and non-blocking lighweight
  * threads which can spawn up to N parallel threads (breadth) and up to M
  * subsequent subthreads (depth)
  *
  * This is for simulation of blocking and non-blocking nature of threadings and
  * their dependencies
  */
abstract sealed class ForkRunner[F[_]: Concurrent: Parallel: Temporal](implicit
    console: Console[F]
) extends Runner[F, Unit] {

  val N: Int // number of parallel threads at root level
  val M: Int // maximum number of level of subthreads
  val chanceBlocking: Double
  val chanceRecursive: Double

  def genThread(
      id: Int,
      level: Int,
      counter: Ref[F, Int]
  ): F[Unit] = {

    val leadingSpace = "..".repeat(level)
    val tailing = if (level > 0) s"- level ${level}" else ""
    val message = s"${leadingSpace}Starting thread #${id} ${tailing}"

    val willSleep = scala.util.Random.nextDouble() < chanceBlocking
    val willGenChild = level < M && scala.util.Random.nextDouble() < chanceRecursive

    val task = for {
      // Tick the global counter
      c <- counter.updateAndGet(_ + 1)

      // Show message
      message_ = s"${message} (counter = $c)"
      _ <- if (level == 0) Screen.green(message_) else Screen.println(message_)

      // Blocking thread
      _ <-
        if (willSleep)
          Screen.yellow(s"${leadingSpace}Thread #${id} level${level} will sleep") >>
          Temporal[F].sleep(1.seconds) >> Screen.cyan(
            s"${leadingSpace}Thread #${id} level${level} woke up from sleep"
          )
        else Monad[F].unit

      // Generate next level thread
      _ <-
        if (willGenChild)
          genThread(
            id,
            level + 1,
            counter
          )
        else Monad[F].unit
    } yield ()

    for {
      fiber <- Concurrent[F].start(task)
    } yield ()
  }

  override def run: F[Unit] = {
    // Generate N threads in parallel
    // root thread never blocks
    for {
      counter <- Ref.of[F, Int](0)
      _ <- (0 to N).toList
        .parTraverse { n => genThread(n, 0, counter) }
        .map(_ => ())
    } yield ()
  }

}

object ForkRunner {
  def make[F[_]: Concurrent: Parallel: Temporal](
      _runParams: Option[ForkParams]
  )(implicit console: Console[F]): ForkRunner[F] = new ForkRunner[F] {
    override val runParams = _runParams
    override val N: Int = _runParams.map(_.N).getOrElse(5)
    override val M: Int = _runParams.map(_.M).getOrElse(5)
    override val chanceBlocking: Double =
      _runParams.map(_.probBlocking).getOrElse(0.0)
    override val chanceRecursive: Double =
      runParams.map(_.probRecursive).getOrElse(0.0)
  }
}
