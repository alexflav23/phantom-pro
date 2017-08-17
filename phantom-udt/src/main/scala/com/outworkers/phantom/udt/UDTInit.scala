package com.outworkers.phantom.udt

import com.outworkers.phantom.dsl.KeySpace
import shapeless.{::, Generic, HList, HNil, Lazy}

/*
trait UDTInit[T] {

  def statements: ExecutableStatementList[Seq]
}

object UDTInit {

  def apply[T : UDTInit]: UDTInit[T] = implicitly[UDTInit[T]]

  def init[V1, HL <: HList](input: V1)(
    implicit gen: Generic.Aux[V1, HL],
    initer: UDTInit[HL]
  ): UDTInit[V1] = new UDTInit[V1] {
    override def statements: ExecutableStatementList[Seq] = initer.statements
  }

  implicit val hnilIniter: UDTInit[HNil] = new UDTInit[HNil] {
    override def statements: ExecutableStatementList[Seq] = new ExecutableStatementList[Seq](Seq.empty)
  }

  implicit def hconsIniter[T, HL <: HList](
    implicit ev: UDTPrimitive[T],
    hconsEv: Lazy[UDTInit[HL]],
    keyspace: KeySpace
  ): UDTInit[T :: HL] = new UDTInit[T :: HL] {
    override def statements: ExecutableStatementList[Seq] = {
      hconsEv.value.statements ++ Seq(ev.schemaQuery())
    }
  }

  implicit def materialize[T, HL <: HList]()(
    implicit ev: Generic.Aux[T, HL],
    initer: Lazy[UDTInit[HL]]
  ): UDTInit[T] = new UDTInit[T] {
    override def statements: ExecutableStatementList[Seq] = initer.value.statements
  }
}*/