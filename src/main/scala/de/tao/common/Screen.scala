package de.tao.common

import cats.Applicative
import cats.effect.std.{Console => CLI}
import cats.implicits._

import scala.{Console => C}

object Screen {

  def println[F[_]](text: String)(implicit eff: CLI[F]) = CLI[F].println(text)

  def printList[F[_]: Applicative, A](list: List[A], max: Option[Int] = None)(
      implicit eff: CLI[F]
  ) = {
    // Sequence List[F[_]] => F[List[_]]
    val N = max.getOrElse(list.size)
    list
      .take(N)
      .map { el => CLI[F].println(el) }
      .sequence *> (if (N < list.size)
                      CLI[F].println(s"... (${list.size - N} more) ...")
                    else Applicative[F].unit)
  }

  def red[F[_]](text: String)(implicit eff: CLI[F]) =
    CLI[F].println(s"${C.RED}${text}${C.RESET}")

  def yellow[F[_]](text: String)(implicit eff: CLI[F]) =
    CLI[F].println(s"${C.YELLOW}${text}${C.RESET}")

  def green[F[_]](text: String)(implicit eff: CLI[F]) =
    CLI[F].println(s"${C.GREEN}${text}${C.RESET}")

  def blue[F[_]](text: String)(implicit eff: CLI[F]) =
    CLI[F].println(s"${C.BLUE}${text}${C.RESET}")

  def cyan[F[_]](text: String)(implicit eff: CLI[F]) =
    CLI[F].println(s"${C.CYAN}${text}${C.RESET}")

  def magenta[F[_]](text: String)(implicit eff: CLI[F]) =
    CLI[F].println(s"${C.MAGENTA}${text}${C.RESET}")
}
