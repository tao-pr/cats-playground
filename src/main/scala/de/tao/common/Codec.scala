package de.tao.common

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
    // Either.catchNonFatal(coder(raw))
    ??? // taotodo
  }

  override def decode(coded: String): Either[Throwable, K] = {
    val tokens = coded.split(delim)
    // parser(tokens).asRight[K]
    ??? // taotodo
  }
}
