package de.tao.runner

import de.tao.config.ForkParams
import de.tao.common.Screen

import cats.effect.Concurrent
import cats.effect.std.Console
import cats.Parallel
import cats.syntax.all._
import cats._

/** ForkRunner runs a hierarchy of mixed blocking and non-blocking lighweight
  * threads which can spawn up to N parallel threads (breadth) and up to M
  * subsequent subthreads (depth)
  *
  * This is for simulation of blocking and non-blocking nature of threadings and
  * their dependencies
  */
abstract sealed class ForkRunner[F[_]: Concurrent: Parallel](implicit
    console: Console[F]
) extends Runner[F, Unit] {

  val N: Int // number of parallel threads at root level
  val M: Int // maximum number of level of subthreads

  def genThread(id: Int, level: Int, blocking: Boolean): F[Unit] = {

    val leadingSpace = "..".repeat(level)
    val message = s"${leadingSpace}Starting thread #{id}"

    for {
      _ <- if (level == 0) Screen.green(message) else Screen.println(message)
    } yield ()
  }

  override def run: F[Unit] = {
    // Generate N threads in parallel
    // root thread never blocks
    (0 to N).toList.parTraverse { n => genThread(n, 0, false) }.map(_ => ())
  }

}

object ForkRunner {
  def make[F[_]: Concurrent: Parallel](
      _runParams: Option[ForkParams]
  )(implicit console: Console[F]): ForkRunner[F] = new ForkRunner[F] {
    override val runParams = _runParams
    override val N: Int = _runParams.map(_.N).getOrElse(5)
    override val M: Int = _runParams.map(_.M).getOrElse(5)
  }
}
