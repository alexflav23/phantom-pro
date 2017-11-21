/*
 * Copyright (C) 2012 - 2017 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 6/2017.
 */
package com.outworkers.phantom

import com.outworkers.phantom.builder.primitives.Primitive

package object autotables {

  implicit class PrimitiveObjectAug(val p: Primitive.type) extends AnyVal {
    def tupled[T <: Product] = new Derived[T]
  }
}
