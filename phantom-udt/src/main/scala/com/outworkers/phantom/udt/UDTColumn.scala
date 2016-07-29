package com.outworkers.phantom.udt

import com.datastax.driver.core.Row
import com.outworkers.phantom.udt.query.UDTCreateQuery
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.connectors.SessionAugmenterImplicits
import com.websudos.phantom.dsl.{KeySpace, Session}

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.column.Column

import scala.util.Try
import scala.reflect.runtime.universe._

abstract class UDTPrimitive[T <: Product with Serializable : TypeTag] extends SessionAugmenterImplicits {

  def instance: T

  def schema()(implicit keySpace: KeySpace, session: Session): CQLQuery = {
    val fields = SchemaGenerator.classAccessors[T]
    val types = SchemaGenerator.infer(instance)

    val inferred = (fields zip types) map {
      case (name, tp) => s"$name $tp"
    } mkString ", "

    if (session.v3orNewer) {
      CQLQuery(s"CREATE TYPE IF NOT EXISTS ${keySpace.name}.test2 $inferred")
    } else if (session.v4orNewer) {
      CQLQuery(s"CREATE TYPE IF NOT EXISTS ${keySpace.name}.test2 $inferred")
    } else {
      CQLQuery(s"CREATE TYPE IF NOT EXISTS ${keySpace.name}.test2 $inferred")
    }
  }

  def fromRow(row: Row): Try[T]

  def name: String

  def asCql(udt: T): String
}


class UDTColumn[
  T <: CassandraTable[T, R],
  R,
  ValueType : UDTPrimitive : TypeTag
](table: CassandraTable[T, R]) extends Column[T, R, ValueType](table) {

  val primitive = implicitly[UDTPrimitive[ValueType]]

  override def parse(row: Row): Try[ValueType] = primitive.fromRow(row)

  override def asCql(v: ValueType): String = primitive.asCql(v)

  override def cassandraType: String = primitive.name

  def create()(implicit keySpace: KeySpace): UDTCreateQuery.Default[T, R] = {
    UDTCreateQuery(table.asInstanceOf[T], primitive.schema())
  }

}
