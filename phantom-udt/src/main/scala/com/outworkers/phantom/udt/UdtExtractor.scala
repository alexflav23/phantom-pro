package com.outworkers.phantom.udt

import com.datastax.driver.core.UDTValue
import shapeless.{Generic, HList, HNil, :: => #:}

import scala.util.{Failure, Success, Try}

trait UdtExtractor[L <: HList] { def apply(row: List[String])(implicit udt: UDTValue): Try[L] }

object UdtExtractor {

  def apply[L <: HList](implicit fromRow: UdtExtractor[L]): UdtExtractor[L] = fromRow

  def fromFunc[L <: HList](f: List[String] => Try[L])(implicit udt: UDTValue) = new UdtExtractor[L] {
    def apply(row: List[String])(implicit udt: UDTValue) = f(row)
  }

  implicit def hnilUdtExtractor()(implicit udt: UDTValue): UdtExtractor[HNil] = fromFunc {
    case Nil => Success(HNil)
    case _ => Failure(new RuntimeException("No more rows expected"))
  }

  implicit def hconsUdtExtractor[H : Extractor, T <: HList : UdtExtractor]()(implicit udt: UDTValue): UdtExtractor[H #: T] =
    fromFunc {
      case h :: t => for {
        hv <- Extractor[H].apply(h, udt)
        tv <- UdtExtractor[T].apply(t)
      } yield hv :: tv
      case Nil => Failure(new RuntimeException("Expected more cells"))
    }


  trait UdtParser[A] {
    def apply[L <: HList](fields: List[String])(implicit
      gen: Generic.Aux[A, L],
      fromRow: UdtExtractor[L],
      udt: UDTValue
    ): Try[A] = fromRow(fields).map(gen.from)
  }

  def extractor[A] = new UdtParser[A] {}
}