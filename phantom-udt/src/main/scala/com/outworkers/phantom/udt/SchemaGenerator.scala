package com.outworkers.phantom.udt

import java.util.Date

import com.datastax.driver.core.UDTValue
import com.websudos.phantom.builder.primitives.Primitive
import com.websudos.phantom.dsl.DateTime
import shapeless.ops.hlist.Mapper
import shapeless.ops.hlist.ToList
import shapeless.{Generic, HList, Poly1}

object SchemaGenerator {

  import scala.reflect.runtime.universe._

  def classAccessors[T : TypeTag]: List[String] = typeOf[T].members.collect {
    case m: MethodSymbol if m.isCaseAccessor => m.name.decodedName.toString
  }.toList

  object Schema extends Poly1 {
    implicit def extractor[T : Primitive] = at[T](_ => Primitive[T].cassandraType)
    implicit def intExtractor = at[Int](_ => Primitive[Int].cassandraType)
    implicit def stringExtractor = at[String](_ => Primitive[String].cassandraType)
    implicit def longExtractor = at[Long](_ => Primitive[Long].cassandraType)
    implicit def bigDecimalExtractor = at[Long](_ => Primitive[BigDecimal].cassandraType)
    implicit def dateExtractor = at[Long](_ => Primitive[Date].cassandraType)
    implicit def dateTimeExtractor = at[Long](_ => Primitive[DateTime].cassandraType)
  }

  object Serializer extends Poly1 {,
    implicit def genericExtractor[T : Primitive] = at[T](Primitive[T].asCql(_))
    //implicit def intExtractor = at[Int](Primitive[Int].asCql(_))
    //implicit def stringExtractor = at[String](Primitive[String].asCql(_))
  }

  def fields[V1 <: Product, Out <: HList](v1: V1)(
    implicit gen: Generic.Aux[V1, Out]
  ): Out = gen to v1

  def infer[V1 <: Product, Out <: HList, MapperOut <: HList](v1: V1)(
    implicit gen: Generic.Aux[V1, Out],
      map: Mapper.Aux[Schema.type, Out, MapperOut],
      to: ToList[MapperOut, String]
  ): List[String] = to (gen to v1 map Schema)

  case class WrappedUdt[T : Extractor](fieldName: String, row: UDTValue)

}
