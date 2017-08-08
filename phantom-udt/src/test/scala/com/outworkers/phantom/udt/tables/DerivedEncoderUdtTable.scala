package com.outworkers.phantom.udt.tables

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.udt._

import scala.concurrent.Future

case class DerivedAddress(
  location: Location,
  postcode: String,
  previousLocations: Set[Location]
)

object DerivedAddress {
  implicit val addressPrimitive: UDTPrimitive[DerivedAddress] = deriveUDT[DerivedAddress]
}

case class DerivedEncoderRecord(
  id: UUID,
  addresses: Set[DerivedAddress]
)

abstract class DerivedEncoderUdtTable extends Table[DerivedEncoderUdtTable, DerivedEncoderRecord] {
  object id extends UUIDColumn with PartitionKey
  object addresses extends SetColumn[DerivedAddress]

  def findById(id: UUID): Future[Option[DerivedEncoderRecord]] = {
    select.where(_.id eqs id).one()
  }
}
