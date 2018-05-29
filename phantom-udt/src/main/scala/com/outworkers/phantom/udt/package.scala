/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 6/2017.
 */
package com.outworkers.phantom

import com.outworkers.phantom.builder.query.execution.{ExecutableCqlQuery, QueryCollection}
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.database.Database
import shapeless._

package object udt {

  implicit class DatabaseAugmenter[DB <: Database[DB]](val db: DB) {

  }

  def udts[HL <: HList](udts: HL)(
    implicit ev: UDTInit[HL],
    space: KeySpace
  ): QueryCollection[Seq] = ev.statements

  def deriveUDT[T]: UDTPrimitive[T] = macro macros.DefMacro.materialize[T]
}
