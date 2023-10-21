package de.tao.runner

import de.tao.config.IOBoundParams
import de.tao.common.Screen
import de.tao.common.Disk._

import cats.effect.std.Console
import cats.effect._
import cats.syntax.all._
import cats.Parallel
import scala.annotation.tailrec

import fs2.{Stream, io, text}

import java.nio.file.{StandardOpenOption, Paths, Files => nioFiles}
import java.time.LocalDate

abstract sealed class IOBoundRunner[F[_]: Parallel: Async](
  override val runParams: Option[IOBoundParams]
)(implicit
    console: Console[F]
) extends Runner[F, Any] {

  val dir = runParams.map(_.dir).getOrElse("./data")
  val N = runParams.map(_.nThreads).getOrElse(5)
  val timeout = runParams.map(_.timeout).getOrElse(100) // ms
  val maxSeqLen = runParams.map(_.maxSeqLen).getOrElse(20)

  override def run: F[Any] = {

    for {
      _ <- Screen.green(s"Generating ${N} CPU-bounded threads")

      // Inspect threadpool
      ec <- Async[F].executionContext
      _ <- Screen.println(s".. execution context: ${ec}")

      // cpu-bound threads, which spawn IO-blocking thread (fiber)
      seqs <- (1 to N).toList.parTraverse(n => genSeq(n))

      _ <- Screen.println(s".. Got final output ${seqs.length} sequences")

    } yield seqs
  }

  def genSeq(n: Int): F[Seq[Byte]] = {

    // Task #1
    // Async task which keeps appending a sequence
    // until the last 3 elements are in descending order.
    val genSeq = for {
      _ <- Screen.println(s"genSeq[Byte] : thread #${n}")
      (a, b) = (
        scala.util.Random.nextBytes(1).head,
        scala.util.Random.nextBytes(1).head
      )
      seq <- Sync[F].delay(genSeqUntilDesc(Seq(a, b), (a, b)))
    } yield (seq)

    // Task #2
    // IO Blocking task which writes a timestamp to a text file
    val writeFile = for {
      _ <- Screen.println(s"Writing timestamp (#${n}) to ${dir}")
      filepath = Paths.get(dir).resolve(Paths.get(s"ts-$n.txt"))
      dt = LocalDate.now().toString()

      // Make sure dir exists
      isCreated <- makeDirExist(dir)

      // write timestamp to file
      // using Sync[F].blocking to shift the operation
      // into another threadpool to avoid blocking the main thread
      _ <- Sync[F].blocking(nioFiles.write(filepath, dt.getBytes))

    } yield ()

    // Now run these 2 tasks (take only result from task #1)
    writeFile >> genSeq
  }

  @tailrec
  def genSeqUntilDesc(ns: Seq[Byte], lastTwo: (Byte, Byte)): Seq[Byte] = {
    val (a, b) = lastTwo
    val c = scala.util.Random.nextBytes(1).head
    if (ns.length >= maxSeqLen - 1 || (a > b && b > c))
      ns.appended(c)
    else {
      genSeqUntilDesc(ns.appended(c), (b, c))
    }
  }

}

object IOBoundRunner {
  def make[F[_]: Console: Parallel: Async](
      params: Option[IOBoundParams]
  ): IOBoundRunner[F] =
    new IOBoundRunner[F](params){}
}
