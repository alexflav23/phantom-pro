package com.outworkers.phantom.udt

import com.datastax.driver.core.Row
import com.websudos.phantom.builder.primitives.Primitive

import scala.util.Try

abstract class UDTPrimitive[T] {

  def fromRow(row: Row): Try[T]

  def name: String

  def asCql(udt: T): String
}


case class Test(id: Int, name: String)

case class Test2(id: Int, name: String)

object Test2 {
  implicit object Test2UdtPrimitive extends UDTPrimitive[Test2] {
    override def fromRow(row: Row): Try[Test2] = {
      for {
        id <- Primitive[Int].fromRow("id", row)
        str <- Primitive[String].fromRow("name", row)
      } yield Test2(id, str)
    }

    override def name: String = "Test2"

    override def asCql(udt: Test2): String =
      s"""{
          |'id': "${Primitive[Int].asCql(udt.id)},
          |'name': ${Primitive[String].asCql(udt.name)},
          |}""".stripMargin
  }
}

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.column.Column

import scala.util.Try

class UDTColumn[
  T <: CassandraTable[T, R],
  R,
  ValueType
](table: CassandraTable[T, R])(implicit primitive: UDTPrimitive[ValueType])
  extends Column[T, R, ValueType](table) {

  override def parse(row: Row): Try[ValueType] = primitive.fromRow(row)

  override def asCql(v: ValueType): String = primitive.asCql(v)

  override def cassandraType: String = primitive.name
}
