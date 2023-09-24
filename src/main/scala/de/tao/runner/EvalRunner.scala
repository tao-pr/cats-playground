package de.tao.runner

import de.tao.config.EvalParams
import de.tao.common.Screen

import cats.effect.std.Console
import cats._
import cats.data.NonEmptyList
import cats.effect.kernel.Ref
import cats.effect.Concurrent
import cats.syntax.all._
import cats.effect.kernel.Async
import cats.effect.kernel.Outcome
import cats.data.OptionT
import cats.instances.vector

abstract sealed class EvalRunner[F[_]: Concurrent: Async](implicit
    console: Console[F]
) extends Runner[F, List[Double]] {

  val evalMode: String
  val vectorSize: Int
  val numThreads: Int

  def genNEL: () => NonEmptyList[Byte] = {
    scala.util.Random.nextBytes(vectorSize).toList match {
      case head :: tail => () => NonEmptyList(head, tail)
      case ks => () => 
        NonEmptyList(0, List.fill[Byte](vectorSize - 1)(0))
    }
  }

  // Three types of thunks
  val thunkLazyAlways = Eval.always(genNEL) // lazy, non-memoized
  val thunkLazyMemoized = Eval.later(genNEL) // lazy, memoized
  val thunkEager = Eval.now(genNEL) // evaluate rightaway, won't work with class vars now

  // Create fiber
  def runThread(
      threadId: Int,
      thunk: Eval[() => NonEmptyList[Byte]]
  ): F[NonEmptyList[Byte]] = {
    for {
      _ <- Screen.println(s"Generating NEL from thread #${threadId}")
      nel <- Async[F].delay(
        thunk.value()
      )
    } yield nel
  }

  def convertOutcome(
      outcome: Outcome[F, Throwable, NonEmptyList[Byte]]
  ): OptionT[F, Double] = outcome match {
    case Outcome.Succeeded(fa) =>
      OptionT(fa.map(nel => Some(nel.toList.sum / nel.size.toDouble)))
    case Outcome.Canceled() => OptionT.none[F, Double]
    case Outcome.Errored(_) => OptionT.none[F, Double]
  }

  override def run: F[List[Double]] = {
    val thunk = evalMode match {
      case "eager"  => thunkEager
      case "always" => thunkLazyAlways
      case "later"  => thunkLazyMemoized
      case _        => thunkEager // eager by default
    }
    val genThreads = (0 until numThreads).toList
      .traverse(i =>
        Concurrent[F].start(
          runThread(i, thunk)
        )
      ) // create fibers

    for {
      _ <- Screen.green(s"Generating ${numThreads} threads")

      // Create list of fibers
      fibers <- genThreads

      // Run fibers and get list of Outcomes
      outcomes <- fibers.traverse(_.join)

      // Map outcomes (of NEL) into list of optional doubles for final return
      doubles <- outcomes.map(convertOutcome).traverse(_.value)
      _ <- Screen.printList(doubles, max = Some(25))

    } yield doubles.flatten
  }

}

object EvalRunner {
  def make[F[_]: Concurrent: Console: Async](
      _runParams: Option[EvalParams]
  ): EvalRunner[F] = new EvalRunner[F] {

    override val runParams = _runParams
    override val evalMode: String = runParams.map(_.evalMode).getOrElse("eager")
    override val vectorSize: Int = runParams.map(_.vectorSize).getOrElse(5)
    override val numThreads: Int = runParams.map(_.numThreads).getOrElse(10)
  }
}
