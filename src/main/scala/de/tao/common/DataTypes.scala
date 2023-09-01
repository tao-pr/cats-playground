package de.tao.common

object DataTypes {
    
  case class SampleCsv(uuid: String, a: Double, b: Double, c: Double, d: Double)

  implicit val sampleCsvCodec = new CsvCodec[SampleCsv] {

    override val parser: List[String] => SampleCsv = {
      ??? // taotodo
    }
    override val coder: SampleCsv => String = {
      ??? // taotodo
    }

  }

}
