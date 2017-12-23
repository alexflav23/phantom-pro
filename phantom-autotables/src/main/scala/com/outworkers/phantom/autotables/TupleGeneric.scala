/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 06/2017.
 */
package com.outworkers.phantom.autotables

import shapeless._
import ops.hlist.Tupler

trait TupleGeneric[C <: Product] extends Serializable {
  type Repr <: Product

  def to(t : C) : Repr

  def from(r : Repr) : C
}

object TupleGeneric {
  type Aux[C <: Product, R] = TupleGeneric[C] { type Repr = R }

  def apply[C <: Product](implicit tgc: TupleGeneric[C]): Aux[C, tgc.Repr] = tgc

  implicit def mkTG[C <: Product, L <: HList, R <: Product](
    implicit cGen: Generic.Aux[C, L],
    tup: Tupler.Aux[L, R],
    tGen: Generic.Aux[R, L]
  ): Aux[C, R] =
    new TupleGeneric[C] {
      type Repr = R

      def to(t : C) : Repr = cGen.to(t).tupled

      def from(r : Repr) : C = cGen.from(tGen.to(r))
    }
}