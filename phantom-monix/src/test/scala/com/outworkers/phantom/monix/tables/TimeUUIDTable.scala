package com.outworkers.phantom.monix.tables

import com.outworkers.phantom.monix._
import org.joda.time.DateTime

import monix.eval.Task

case class TimeUUIDRecord(
  user: UUID,
  id: UUID,
  name: String
) {
  def timestamp: DateTime = id.datetime
}

abstract class TimeUUIDTable extends Table[TimeUUIDTable, TimeUUIDRecord] {

  object user extends UUIDColumn with PartitionKey

  object id extends TimeUUIDColumn with ClusteringOrder with Descending

  object name extends StringColumn

  def retrieve(user: UUID): Task[List[TimeUUIDRecord]] = {
    select.where(_.user eqs user).orderBy(_.id ascending).fetch()
  }

  def retrieveDescending(user: UUID): Task[List[TimeUUIDRecord]] = {
    select.where(_.user eqs user).orderBy(_.id descending).fetch()
  }
}