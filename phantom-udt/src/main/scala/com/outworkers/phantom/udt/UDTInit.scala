package com.outworkers.phantom.udt

import com.outworkers.phantom.builder.query.execution.QueryCollection
import com.outworkers.phantom.dsl.KeySpace
import shapeless.{::, Generic, HList, HNil, Lazy}


trait UDTInit[T] {

  def statements: QueryCollection[Seq]
}

object UDTInit {

  def apply[T: UDTInit]: UDTInit[T] = implicitly[UDTInit[T]]

  def init[V1, HL <: HList](input: V1)(
    implicit gen: Generic.Aux[V1, HL],
    initer: UDTInit[HL]
  ): UDTInit[V1] = new UDTInit[V1] {
    override def statements: QueryCollection[Seq] = initer.statements
  }

  implicit val hnilIniter: UDTInit[HNil] = new UDTInit[HNil] {
    override def statements: QueryCollection[Seq] = new QueryCollection[Seq](Seq.empty)
  }

  implicit def hconsIniter[T, HL <: HList](
    implicit ev: UDTPrimitive[T],
    hconsEv: UDTInit[HL],
    keyspace: KeySpace
  ): UDTInit[T :: HL] = new UDTInit[T :: HL] {
    override def statements: QueryCollection[Seq] = {
      hconsEv.statements appendAll Seq(ev.schemaQuery())
    }
  }
}