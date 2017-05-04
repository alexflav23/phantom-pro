package com.outworkers.phantom.autotables
package tables

import com.outworkers.phantom.dsl._

import scala.concurrent.Future

abstract class Users extends Table[Users, User] with RootConnector {
  object id extends UUIDColumn(this) with PartitionKey
  object email extends StringColumn(this)
  object location extends Col[Location](this)

  def findById(id: UUID): Future[Option[User]] = select.where(_.id eqs id).one()
}


