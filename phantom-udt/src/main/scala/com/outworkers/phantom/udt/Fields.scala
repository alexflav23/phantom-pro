package com.outworkers.phantom.udt

/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
import java.net.InetAddress
import java.util.{Date, UUID}

import org.joda.time.DateTime
import com.datastax.driver.core.{LocalDate, UDTValue}
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.primitives.Primitive

import scala.reflect.runtime.{currentMirror => cm, universe => ru}
import scala.util.{DynamicVariable, Try}

/**
  * A field part of a user defined type.
  *
  * @param owner The UDT column that owns the field.
  * @tparam T The Scala type corresponding the underlying Cassandra type of the UDT field.
  */
sealed abstract class AbstractField[
  @specialized(Int, Double, Float, Long, Boolean, Short) T : Primitive
](owner: UDTColumn[_, _, _]) {

  lazy val name: String = cm.reflect(this).symbol.name.toTypeName.decodedName.toString

  protected[udt] lazy val valueBox = new DynamicVariable[Option[T]](None)

  def value: T = valueBox.value.getOrElse(null.asInstanceOf[T])

  private[udt] def setSerialise(data: UDTValue): UDTValue

  private[udt] def set(value: Option[T]): Unit = valueBox.value_=(value)

  private[udt] def set(data: UDTValue): Unit = valueBox.value_=(apply(data))

  def cassandraType: String = Primitive[T].cassandraType

  def apply(row: UDTValue): Option[T]
}


private[udt] abstract class Field[
  Owner <: CassandraTable[Owner, Record],
  Record,
  FieldOwner <: UDTColumn[Owner, Record, _],
  T : Primitive
](column: FieldOwner) extends AbstractField[T](column) {}

object PrimitiveBoxedManifests {
  val StringManifest = manifest[String]
  val IntManifest = manifest[Int]
  val DoubleManifest = manifest[Double]
  val LongManifest = manifest[Long]
  val FloatManifest = manifest[Float]
  val BigDecimalManifest = manifest[BigDecimal]
  val BigIntManifest = manifest[BigInt]
  val DateManifest = manifest[Date]
}

sealed trait Extractor[T] {
  def apply(name: String, udt: UDTValue): Try[T]
}

object Extractor {
  implicit case object BooleanExtractor extends Extractor[Boolean] {
    def apply(name: String, udt: UDTValue): Try[Boolean] = Try(udt.getBool(name))
  }

  implicit case object StringExtractor extends Extractor[String] {
    def apply(name: String, udt: UDTValue): Try[String] = Try(udt.getString(name))
  }

  implicit case object InetExtractor extends Extractor[InetAddress] {
    def apply(name: String, udt: UDTValue): Try[InetAddress] = Try(udt.getInet(name))
  }

  implicit case object IntExtractor extends Extractor[Int] {
    def apply(name: String, udt: UDTValue): Try[Int] = Try(udt.getInt(name))
  }

  def apply[T : Extractor]: Extractor[T] = implicitly[Extractor[T]]
}

object Fields {

  class BooleanField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T) extends Field[Owner, Record, T,
    Boolean](column) {

    def apply(row: UDTValue): Option[Boolean] = Some(row.getBool(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setBool(name, value)
  }

  class StringField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T) extends Field[Owner, Record, T, String](column) {
    def apply(row: UDTValue): Option[String] = Some(row.getString(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setString(name, value)
  }

  class InetField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T)  extends Field[Owner, Record, T, InetAddress](column) {
    def apply(row: UDTValue): Option[InetAddress] = Some(row.getInet(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setInet(name, value)
  }

  class IntField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T)  extends Field[Owner, Record, T, Int](column) {
    def apply(row: UDTValue): Option[Int] = Some(row.getInt(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setInt(name, value)
  }

  class DoubleField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T)  extends Field[Owner, Record, T, Double](column) {
    def apply(row: UDTValue): Option[Double] = Some(row.getDouble(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setDouble(name, value)
  }

  class LongField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T)  extends Field[Owner, Record, T, Long](column) {
    def apply(row: UDTValue): Option[Long] = Some(row.getLong(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setLong(name, value)
  }

  class BigIntField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T)  extends Field[Owner, Record, T, BigInt](column) {
    def apply(row: UDTValue): Option[BigInt] = Some(row.getVarint(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setVarint(name, value.bigInteger)
  }

  class BigDecimalField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T)  extends Field[Owner, Record, T, BigDecimal](column) {
    def apply(row: UDTValue): Option[BigDecimal] = Some(row.getDecimal(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setDecimal(name, value.bigDecimal)
  }

  class DateField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T) extends Field[Owner, Record, T, Date](column) {
    def apply(row: UDTValue): Option[Date] = Some(new Date(row.getDate(name).getMillisSinceEpoch))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = {
      data.setDate(name, LocalDate.fromMillisSinceEpoch(value.getTime))
    }
  }

  class DateTimeField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T) extends Field[Owner, Record, T, DateTime](column) {
    def apply(row: UDTValue): Option[DateTime] = Some(new DateTime(row.getDate(name)))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = {
      data.setDate(name,  LocalDate.fromMillisSinceEpoch(value.getMillis))
    }
  }

  /*
  class UDTField[Owner <: UDTColumn[Owner, ], T <: UDTColumn[_]](column: Owner) extends Field[Owner, T](column) {
    def apply(row: Row): DateTime = new DateTime(row.getDate(name))
  }*/

  class UUIDField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T) extends Field[Owner, Record, T, UUID](column) {
    def apply(row: UDTValue): Option[UUID] = Some(row.getUUID(name))

    override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setUUID(name, value)
  }
}
