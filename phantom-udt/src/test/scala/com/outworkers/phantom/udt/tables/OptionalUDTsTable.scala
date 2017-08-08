package com.outworkers.phantom.udt.tables

import com.outworkers.phantom.dsl._
import com.outworkers.phantom.udt.domain.OptionalUdt

import scala.concurrent.Future

case class OptionUDTRecord(
  id: UUID,
  opt: OptionalUdt,
  col: List[OptionalUdt],
  colSet: Set[OptionalUdt],
  map: Map[String, OptionalUdt]
)

abstract class OptionalUDTsTable extends Table[OptionalUDTsTable, OptionUDTRecord] {
  object id extends UUIDColumn with PartitionKey
  object opt extends Col[OptionalUdt]
  object col extends ListColumn[OptionalUdt]
  object colSet extends SetColumn[OptionalUdt]
  object map extends MapColumn[String, OptionalUdt]

  def findById(id: UUID): Future[Option[OptionUDTRecord]] = {
    select.where(_.id eqs id).one()
  }
}
