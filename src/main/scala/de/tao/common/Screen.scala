package de.tao.common

import cats.effect.std.{Console => CLI}
import scala.{Console => C}

object Screen {
  def println[F[_]](text: String)(implicit eff: CLI[F]) = CLI[F].println(text)
  def red[F[_]](text: String)(implicit eff: CLI[F]) = CLI[F].println(s"${C.RED}${text}${C.RESET}")
  def green[F[_]](text: String)(implicit eff: CLI[F]) = CLI[F].println(s"${C.GREEN}${text}${C.RESET}")
  def blue[F[_]](text: String)(implicit eff: CLI[F]) = CLI[F].println(s"${C.BLUE}${text}${C.RESET}")
  def cyan[F[_]](text: String)(implicit eff: CLI[F]) = CLI[F].println(s"${C.CYAN}${text}${C.RESET}")
  def magenta[F[_]](text: String)(implicit eff: CLI[F]) = CLI[F].println(s"${C.MAGENTA}${text}${C.RESET}")
}
