package de.tao.runner

import de.tao.config.SemaphoreParams
import de.tao.common.Screen

import cats.effect.std.Console
import cats.effect._
import cats._
import cats.effect.std.Semaphore
import cats.syntax.all._
import cats.Monad

import fs2.Stream

import scala.concurrent.duration._

class ResourcePool[F[_]: Async: Console: Temporal](semaphore: Semaphore[F]) {

  def use(n: Int, i: Int): F[Unit] = {
    for {
      _ <- Screen.println(s"Thread ${i} requesting Semaphore (${n} blocks)")
      acquired <- semaphore.tryAcquireN(n)

      // Use acquired resource and release
      _ <-
        if (acquired) {
          Screen.green(s"Thread ${i} acquiring Semaphore (${n})") *>
            Temporal[F].sleep(250.milli) *>
            semaphore.release *>
            Screen.cyan(s"Thread ${i} releasing Semaphore (${n})") *>
            Monad[F].unit
        } else Monad[F].unit

    } yield ()
  }

}

sealed abstract class SemaphoreRunner[F[_]: Async: Temporal](implicit
    console: Console[F]
) extends Runner[F, Unit] {

  val maxShared: Int

  override def run: F[Unit] = {
    for {
      semaphore <- Semaphore[F](maxShared)
      pool = new ResourcePool[F](semaphore)
      counter <- Ref.of[F, Int](0)
    } yield Stream
      .awakeEvery(500.millis)
      .evalMap(_ => spawn(pool, counter))
      .compile
      .drain
  }

  def spawn(pool: ResourcePool[F], counter: Ref[F, Int]): F[Unit] = {

    val size = 1 + (scala.util.Random.nextInt() % 4).abs.toInt

    for {
      c <- counter.updateAndGet(_ + 1)
      _ <- Screen.println(s"Spawning thread ${c}")
      _ <- pool.use(size, c)
    } yield ()
  }

}

object SemaphoreRunner {
  def make[F[_]: Async: Console: Temporal](config: Option[SemaphoreParams]) =
    new SemaphoreRunner[F] {
      override val runParams = None
      override val maxShared: Int = config.map(_.maxShared).getOrElse(2)
    }
}
