package com.outworkers.phantom.autotables
package tables

import com.outworkers.phantom.dsl._

import scala.concurrent.Future

abstract class Users extends Table[Users, User] {
  object id extends UUIDColumn with PartitionKey
  object email extends StringColumn
  object location extends Col[Location]
  object previousLocations extends SetColumn[Location]

  def findById(id: UUID): Future[Option[User]] = select.where(_.id eqs id).one()
}


