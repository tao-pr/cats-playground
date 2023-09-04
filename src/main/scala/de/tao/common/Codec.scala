package de.tao.common

import cats.syntax.EitherOps
import cats.syntax.either._

import io.circe._
import io.circe.syntax._


/**
  * Defines a code which transform between coded [C] and raw type [K]
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

abstract class JsonCodec[K] extends StringCodec[K]{

  // circe
  implicit val jsonEncoder: Encoder[K]

  val parser: String => K = throw new NotImplementedError("parser is not implemented")
  val coder: K => String = (k: K) => k.asJson.noSpaces

  override def encode(raw: K): Either[Throwable, String] = {
    Either.catchNonFatal{ coder(raw) }
  }

  override def decode(coded: String): Either[Throwable, K] = {
    Either.catchNonFatal{ parser(coded) }
  }
}
