/*
 * Copyright (C) 2012 - 2017 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 6/2017.
 */
package com.outworkers.phantom

import com.outworkers.phantom.builder.query.QueryOptions
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.execution.{ExecutableCqlQuery, QueryCollection}
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.udt.macros.DefMacro
import shapeless._
import shapeless.ops.hlist._

package object udt {

  implicit class DatabaseAugmenter[DB <: Database[DB]](val db: DB) {

    object ExtractSchema extends Poly1 {
      implicit def caseGeneric[T <: Product with Serializable : UDTPrimitive]: Case.Aux[T, ExecutableCqlQuery] = {
        at(el => implicitly[UDTPrimitive[T]].schemaQuery()(db.space))
      }
    }

    def initUdts[HL <: HList, Rev <: HList, Out <: HList](hl: HL)(
      implicit rev: Reverse.Aux[HL, Rev],
      mapped: Mapper.Aux[ExtractSchema.type, Rev, Out],
      toList: ToList[Out, CQLQuery]
    ): QueryCollection[Seq] = new QueryCollection[Seq](
      toList(rev(hl).map(ExtractSchema)).map(
        qb => ExecutableCqlQuery(qb, QueryOptions.empty)
      )
    )
  }

  def deriveUDT[T]: UDTPrimitive[T] = macro DefMacro.materialize[T]
}
