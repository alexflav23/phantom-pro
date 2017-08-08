package com.outworkers.phantom.udt.tables

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.udt.Udt

import scala.concurrent.Future

@Udt case class ChargeDuration(
  coarseDuration: Double,
  sliceDuration: Double,
  units: String,
  price: Double
)

@Udt case class MaxDuration(
  duration: Double,
  units: String
)

@Udt case class ParkingCharge(
  description: Option[String],
  chargeDuration: Set[ChargeDuration],
  maxDuration: Option[MaxDuration],
  maxCharge: Option[Double]
)

case class Policy(
  uid: String,
  policyAuthorizerid: String,
  parkingCharge: Set[ParkingCharge]
)

abstract class AuthorizerPolicy extends Table[AuthorizerPolicy, Policy] {
  object uid extends StringColumn with PartitionKey
  object policyAuthorizerid extends StringColumn
  object parkingCharge extends SetColumn[ParkingCharge]

  def findById(uid: String): Future[Option[Policy]] = {
    select.where(_.uid eqs uid).one()
  }
}
