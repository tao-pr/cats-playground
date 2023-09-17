package de.tao.common

import io.circe.{Encoder, Decoder}
import io.circe.syntax._
import io.circe.generic.semiauto._
import cats.kernel.Monoid

object DataTypes {

  case class SampleCsv(uuid: String, a: Double, b: Double, c: Double, d: Double)

  // Unsafe codec
  implicit val sampleCsvCodec = new CsvCodec[SampleCsv] {

    override val parser: List[String] => SampleCsv = { ns =>
      val uuid = ns.head
      val vector = ns.tail
      assert(vector.size == 4)
      val List(a, b, c, d) = vector.map(_.toDouble)
      SampleCsv(uuid, a, b, c, d)
    }
    override val coder: SampleCsv => String = { k =>
      s"${k.uuid},${k.a},${k.b},${k.c},${k.d}"
    }
  }

  implicit val sampleJsonCodec = new JsonCodec[SampleCsv] {
    override implicit val jsonEncoder: Encoder[SampleCsv] =
      deriveCodec[SampleCsv]
    override implicit val jsonDecoder: Decoder[SampleCsv] =
      deriveCodec[SampleCsv]
  }

  // Defining how multiple CSV entries are combined
  val sumSampleCsvM = new Monoid[SampleCsv] {

    override def empty: SampleCsv = SampleCsv("", 0, 0, 0, 0)

    override def combine(x: SampleCsv, y: SampleCsv): SampleCsv = {
      if (x.uuid.isEmpty)
        y
      else if (y.uuid.isEmpty)
        x
      else {
        x.copy(
          a = x.a + y.a,
          b = x.b + y.b,
          c = x.c + y.c,
          d = x.d + y.d
        )
      }
    }
  }
}
