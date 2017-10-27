package com.outworkers.phantom.monix.tables

import com.outworkers.phantom.monix._

case class PrimitiveRecord(
  pkey: String,
  long: Long,
  boolean: Boolean,
  bDecimal: BigDecimal,
  double: Double,
  float: Float,
  inet: java.net.InetAddress,
  int: Int,
  date: java.util.Date,
  uuid: java.util.UUID,
  bi: BigInt
)

abstract class PrimitivesTable extends Table[PrimitivesTable, PrimitiveRecord] {
  object pkey extends StringColumn with PartitionKey

  object long extends LongColumn

  object boolean extends BooleanColumn

  object bDecimal extends BigDecimalColumn

  object double extends DoubleColumn

  object float extends FloatColumn

  object inet extends InetAddressColumn

  object int extends IntColumn

  object date extends DateColumn

  object uuid extends UUIDColumn

  object bi extends BigIntColumn
}
