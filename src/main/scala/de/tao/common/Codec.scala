package de.tao.common

import cats.syntax.EitherOps
import cats.syntax.either._

import io.circe._
import io.circe.syntax._
import io.circe.generic.auto._ // for generic decoder

/** Defines a code which transform between coded [C] and raw type [K]
  */
trait Codec[K, C] {
  def encode(raw: K): Either[Throwable, C]
  def decode(coded: C): Either[Throwable, K]
}

trait StringCodec[K] extends Codec[K, String] {
  override def encode(raw: K): Either[Throwable, String]
  override def decode(coded: String): Either[Throwable, K]
}

abstract class CsvCodec[K](delim: String = ",") extends StringCodec[K] {

  val parser: List[String] => K
  val coder: K => String

  override def encode(raw: K): Either[Throwable, String] = {
    Either.catchNonFatal { coder(raw) }
  }

  override def decode(coded: String): Either[Throwable, K] = {
    Either.catchNonFatal {
      val tokens = coded.split(delim).toList
      parser(tokens)
    }
  }
}

abstract class JsonCodec[K] extends StringCodec[K] {

  // Circle encoder
  implicit val jsonEncoder: Encoder[K]
  implicit val jsonDecoder: Decoder[K]

  override def encode(raw: K): Either[Throwable, String] = {
    Either.catchNonFatal {
      raw.asJson.noSpacesSortKeys
    }
  }

  override def decode(coded: String): Either[Throwable, K] = {
    io.circe.parser.decode[K](coded)
  }
}
