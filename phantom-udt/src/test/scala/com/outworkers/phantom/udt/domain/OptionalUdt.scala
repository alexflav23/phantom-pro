package com.outworkers.phantom.udt.domain

import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.Date
import com.outworkers.phantom.dsl.UUID
import com.outworkers.phantom.udt.{Udt, deriveUDT}

@Udt case class OptionalUdt(
  id: UUID,
  optionalDate: Option[Date],
  optionalBigDecimal: Option[BigDecimal],
  optionalBigInt: Option[BigInt],
  optionalBoolean: Option[Boolean],
  optionalUUID: Option[UUID],
  optionalDouble: Option[Double],
  optionalInt: Option[Int],
  optionalShort: Option[Short],
  optionalFloat: Option[Float],
  optionalLong: Option[Long],
  //optionalDateTime: Option[DateTime],
  //optionalLocalDate: Option[LocalDate],
  optionalString: Option[String],
  optionalByteBuffer: Option[ByteBuffer],
  optionalInet: Option[InetAddress]
)


case class A(value: Int)

object A {
  implicit val primitive = deriveUDT[A]
}

case class B(a: A) {
  implicit val udt = deriveUDT[B]
}
