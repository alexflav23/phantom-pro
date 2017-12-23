/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 06/2017.
 */
package com.outworkers.phantom.autotables

import com.outworkers.phantom.builder.primitives.Primitive

class Derived[T <: Product] {
  def derive[TP](
    implicit tp: TupleGeneric.Aux[T, TP],
    ev: Primitive[TP]
  ): Primitive[T] = {
    Primitive.derive[T, TP](s => tp to s)(in => tp from in)
  }
}
