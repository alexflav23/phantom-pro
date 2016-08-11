package com.outworkers.phantom.udt

import com.datastax.driver.core.Row
import com.outworkers.phantom.udt.SchemaGenerator.Schema
import com.outworkers.phantom.udt.query.UDTCreateQuery
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.column.Column
import com.websudos.phantom.connectors.SessionAugmenterImplicits
import com.websudos.phantom.dsl.{KeySpace, Session}
import shapeless.ops.hlist.{Mapper, ToList, Zip}
import shapeless.ops.traversable.FromTraversable
import shapeless.{::, Generic, HList, HNil}

import scala.reflect.runtime.universe._
import scala.util.{Failure, Success, Try}

abstract class UDTPrimitive[
  T <: Product with Serializable : TypeTag
]() extends SessionAugmenterImplicits {

  def instance: T

  def schema[
    Out <: HList,
    MapperOut <: HList
  ]()(
    implicit keySpace: KeySpace,
    session: Session,
    gen: Generic.Aux[T, Out],
    map: Mapper.Aux[Schema.type, Out, MapperOut],
    to: ToList[MapperOut, String]
  ): CQLQuery = {
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

  def fromRow(row: Row): Option[T]

  def name: String

  def asCql(udt: T): String
}

abstract class UDTColumn[
  T <: CassandraTable[T, R],
  R,
  ValueType <: Product with Serializable : UDTPrimitive : TypeTag
](table: CassandraTable[T, R]) extends Column[T, R, ValueType](table) with SessionAugmenterImplicits {

  val primitive = implicitly[UDTPrimitive[ValueType]]

  def instance: ValueType = primitive.instance

  override def parse(row: Row): Try[ValueType] = primitive.fromRow(row) match {
    case Some(value) => Success(value)
    case None => Failure(new Exception("Couldn't parse UDT"))
  }

  override def asCql(v: ValueType): String = primitive.asCql(v)

  override def cassandraType: String = primitive.name

  def create[
    Out <: HList,
    MapperOut <: HList
  ]()(
    implicit keySpace: KeySpace,
    session: Session,
    gen: Generic.Aux[ValueType, Out],
    map: Mapper.Aux[Schema.type, Out, MapperOut],
    to: ToList[MapperOut, String]
  ): UDTCreateQuery.Default[T, R] = {

    val fields = SchemaGenerator.classAccessors[ValueType]
    val types = SchemaGenerator.infer(instance)

    val inferred = (fields zip types) map {
      case (name, tp) => s"$name $tp"
    } mkString ", "

    val qb = if (session.v3orNewer) {
      CQLQuery(s"CREATE TYPE IF NOT EXISTS ${keySpace.name}.test2 $inferred")
    } else if (session.v4orNewer) {
      CQLQuery(s"CREATE TYPE IF NOT EXISTS ${keySpace.name}.test2 $inferred")
    } else {
      CQLQuery(s"CREATE TYPE IF NOT EXISTS ${keySpace.name}.test2 $inferred")
    }

    UDTCreateQuery(table.asInstanceOf[T], qb)
  }
}
