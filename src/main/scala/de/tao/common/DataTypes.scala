package de.tao.common

import io.circe.Encoder

object DataTypes {
    
  case class SampleCsv(uuid: String, a: Double, b: Double, c: Double, d: Double)

  // Unsafe codec
  implicit val sampleCsvCodec = new CsvCodec[SampleCsv] {

    override val parser: List[String] => SampleCsv = { ns =>
      val uuid = ns.head
      val vector = ns.tail
      assert(vector.size == 4)
      val List(a,b,c,d) = vector.map(_.toDouble)
      SampleCsv(uuid, a, b, c, d)
    }
    override val coder: SampleCsv => String = { k =>
      s"${k.uuid},${k.a},${k.b},${k.c},${k.d}"
    }

  }

  implicit val sampleJsonCodec = new JsonCodec[SampleCsv] {
    override implicit val jsonEncoder: Encoder[SampleCsv] = Encoder[SampleCsv]
  }
}
