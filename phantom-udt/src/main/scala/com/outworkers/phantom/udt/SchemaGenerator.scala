package com.outworkers.phantom.udt

import java.util.Date

import com.datastax.driver.core.{Row, UDTValue}
import com.websudos.phantom.builder.primitives.Primitive
import com.websudos.phantom.dsl.DateTime
import shapeless.ops.hlist.{Mapper, ToList, Zip}
import shapeless.{Generic, HList, Poly1, Zipper}
import shapeless._
import shapeless.ops.traversable.FromTraversable
import syntax.std.traversable._

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

  object results extends Poly1 {
    implicit def extractor[T : Extractor] = at[((T, String), UDTValue)] {
      case ((value, fieldName), row) => Extractor[T].apply(fieldName, row)
    }

    //implicit def intExtractor = at[Int](_ => Primitive[Int].cassandraType)
    //implicit def stringExtractor = at[String](_ => Primitive[String].cassandraType)
    //implicit def longExtractor = at[Long](_ => Primitive[Long].cassandraType)
    //implicit def bigDecimalExtractor = at[Long](_ => Primitive[BigDecimal].cassandraType)
    //implicit def dateExtractor = at[Long](_ => Primitive[Date].cassandraType)
    //implicit def dateTimeExtractor = at[Long](_ => Primitive[DateTime].cassandraType)
  }

  case class WrappedUdt[T : Extractor](fieldName: String, row: UDTValue)

  object Serializer extends Poly1 {
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

  def extractor[
    V1 <: Product,
    Out <: HList,
    ExOut <: HList,
    Fields <: HList,
    RowList <: HList,
    ZippedPair <: HList,
    Result <: HList
  ](v1: V1, row: Row)(
    implicit tag: TypeTag[V1],
      gen: Generic.Aux[V1, Out],
      fl: FromTraversable[Fields],
      fl2: FromTraversable[RowList],
      zipper: Zip.Aux[Out ::  Fields :: HNil, ExOut],
      zipper2: Zip.Aux[ExOut :: RowList :: HNil, ZippedPair],
      ext: Mapper.Aux[results.type, ZippedPair, Result],
      reifier: Generic.Aux[Result, V1]
  ): Option[V1] = {
    for {
      accessors <- Some(classAccessors[V1])
      rows <- fl2(List.tabulate(accessors.size)(_ => row))
      fields <- fl(accessors)
    } yield {
      reifier to ((((gen to v1) zip fields) zip rows) map results)
    }
  }


}
