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
import scala.util.Try

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

  def fromRow(row: Row): Try[T]

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

  override def parse(row: Row): Try[ValueType] = primitive.fromRow(row)

  /**
    * This method will automatically derive an extractor for an UDT value
    * given a target case class and a physical instance of an UDTValue
    * returned from the server.
    * @param v1 A sample instance of the case class to extract, needed to derive an HList of the values.
    * @param row The UDT Value originating from the server that needs to be extracted.
    * @param gen The generic used to convert from the input case class to an HList.
    * @param fl The implicit evidence used to convert the list of fields extracted from the case class
    *           via the typetag, to an HList with a string LUB.
    * @param fl2 The implicit evidence used to convert the artificially made up list of udt values to an
    *            hlist so we can zip it together with the fields and types to map over it with a poly.
    * @param zipper A zipper that can zip together the types of the case class encoded as an HList
    *               with the string field name list transformed to an HList[String :: String ... :: HNil].
    * @param extractor The extractor mapper,
    * @param reifier
    * @tparam Out
    * @tparam ExOut
    * @tparam Fields
    * @tparam RowList
    * @tparam ZippedPair
    * @tparam Result
    * @return
    */
  def extractor[
    Out <: HList,
    ExOut <: HList,
    Fields <: HList,
    RowList <: HList,
    ZippedPair <: HList,
    Result <: HList
  ](v1: ValueType, row: Row)(
    implicit gen: Generic.Aux[ValueType, Out],
    fl: FromTraversable[Fields],
    fl2: FromTraversable[RowList],
    zipper: Zip.Aux[Out ::  Fields :: HNil, ExOut],
    zipper2: Zip.Aux[ExOut :: RowList :: HNil, ZippedPair],
    extractor: Mapper.Aux[SchemaGenerator.results.type, ZippedPair, Result],
    reifier: Generic.Aux[Result, ValueType]
  ): Option[ValueType] = SchemaGenerator.extractor(instance, row)


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
