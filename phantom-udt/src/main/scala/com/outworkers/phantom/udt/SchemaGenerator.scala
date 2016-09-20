package com.outworkers.phantom.udt

import com.datastax.driver.core.{Row, UDTValue}
import com.websudos.phantom.builder.primitives.Primitive
import com.websudos.phantom.dsl.KeySpace
import shapeless.ops.hlist.{Mapper, ToList, Zip, _}
import shapeless.ops.nat.{LT, Mod}
import shapeless.ops.record.Keys
import shapeless.ops.traversable.FromTraversable
import shapeless.{Generic, HList, Poly1, _}

import scala.util.Try

object SchemaGenerator {

  object Schema extends Poly1 {
    implicit def extractor[T : Primitive] = at[T](_ => Primitive[T].cassandraType)
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

  import scala.reflect.runtime.universe.TypeTag

  def schema[V1 <: Product, Out <: HList, MapperOut <: HList](v1: V1)(
    implicit space: KeySpace,
    gen: Generic.Aux[V1, Out],
    map: Mapper.Aux[Schema.type, Out, MapperOut],
    to: ToList[MapperOut, String],
    tag: TypeTag[V1]
  ): String = {

    val udtSchema = (Helper.classAccessors[V1] zip infer[V1, Out, MapperOut](v1)) map {
      case (name, tp) => s"$name $tp"
    } mkString ", "

    s"CREATE TYPE IF NOT EXISTS ${space.name}.${v1.getClass.getSimpleName.toLowerCase} $udtSchema"
  }

  /**
    * This method will automatically derive an extractor for an UDT value
    * given a target case class and a physical instance of an UDTValue
    * returned from the server.
    *
    * @param v1 A sample instance of the case class to extract, needed to derive an HList of the values.
    * @param gen The generic used to convert from the input case class to an HList.
    * @param fl2 The implicit evidence used to convert the artificially made up list of udt values to an
    *            hlist so we can zip it together with the fields and types to map over it with a poly.
    * @param zipper A zipper that can zip together the types of the case class encoded as an HList
    *               with the string field name list transformed to an HList[String :: String ... :: HNil].
    * @param ext The extractor mapper, which maps the resulting tuples to actual types.
    */
  def extractor[
    V1 <: Product,
    Out <: HList,
    ExOut <: HList,
    Fields <: HList,
    RowList <: HList,
    ZippedPair <: HList,
    Result <: HList
  ](v1: V1)(
    implicit gen: LabelledGeneric.Aux[V1, Out],
      keys: Keys.Aux[Out, Fields],
      fl2: FromTraversable[RowList],
      zipper: Zip.Aux[Out :: Fields :: HNil, ExOut],
      zipper2: Zip.Aux[ExOut :: RowList :: HNil, ZippedPair],
      ext: Mapper.Aux[results.type, ZippedPair, Result],
      reifier: Generic.Aux[Result, V1]
  ): Row => Option[V1] = row => {
    for {
      rows <- fl2(List.tabulate(v1.productIterator.size)(_ => row))
    } yield {
      reifier to ((((gen to v1) zip keys.apply()) zip rows) map results)
    }
  }
}
