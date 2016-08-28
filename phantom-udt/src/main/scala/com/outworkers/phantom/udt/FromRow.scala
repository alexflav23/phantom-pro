package com.outworkers.phantom.udt

import com.datastax.driver.core.Row
import com.websudos.phantom.builder.primitives.Primitive
import shapeless._

import scala.util.{Failure, Success, Try}

trait FromRow[L <: HList] { def apply(row: List[(String, Row)]): Try[L] }

object FromRow {
  import HList.ListCompat._

  def apply[L <: HList](implicit fromRow: FromRow[L]): FromRow[L] = fromRow

  def fromFunc[L <: HList](f: List[(String, Row)] => Try[L]) = new FromRow[L] {
    def apply(row: List[(String, Row)]) = f(row)
  }

  implicit val hnilFromRow: FromRow[HNil] = fromFunc {
    case Nil => Success(HNil)
    case _ => Failure(new RuntimeException("No more rows expected"))
  }

  implicit def hconsFromRow[H : Primitive, T <: HList : FromRow]: FromRow[H :: T] =
    fromFunc {
      case h :: t => for {
        hv <- Primitive[H].fromRow(h._1, h._2)
        tv <- FromRow[T].apply(t)
      } yield hv :: tv
      case Nil => Failure(new RuntimeException("Expected more cells"))
    }


  trait RowParser[A] {
    def apply[L <: HList](row: List[(String, Row)])(implicit
      gen: Generic.Aux[A, L],
      fromRow: FromRow[L]
    ): Try[A] = fromRow(row).map(gen.from)
  }

  def rowParserFor[A] = new RowParser[A] {}
}