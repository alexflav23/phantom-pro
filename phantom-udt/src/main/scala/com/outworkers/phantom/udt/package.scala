/*
 * Copyright (C) 2012 - 2017 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.co.uk>, 6/2017.
 */
package com.outworkers.phantom

import com.outworkers.phantom.builder.query.ExecutableStatementList
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.builder.query.engine.CQLQuery
import shapeless._
import shapeless.ops.hlist._

package object udt {

  implicit class TableAugmenter[T <: CassandraTable[T, R], R](val table: CassandraTable[T, R]) extends AnyVal {
    type UDTColumn[ValueType <: Product with Serializable] = com.outworkers.phantom.udt.columns.UDTColumn[T, R, ValueType]
  }

  implicit class DatabaseAugmenter[DB <: Database[DB]](val db: DB) {

    object ExtractSchema extends Poly1 {
      implicit def caseGeneric[T <: Product with Serializable : UDTPrimitive]: Case.Aux[T, CQLQuery] = {
        at(el => implicitly[UDTPrimitive[T]].schemaQuery()(db.space))
      }
    }

    def initUdts[HL <: HList, Rev <: HList, Out <: HList](hl: HL)(
      implicit rev: Reverse.Aux[HL, Rev],
      mapped: Mapper.Aux[ExtractSchema.type, Rev, Out],
      toList: ToList[Out, CQLQuery]
    ): ExecutableStatementList[Seq] = new ExecutableStatementList[Seq](toList(rev(hl).map(ExtractSchema)))
  }

  type UDTColumn[
    Table <: CassandraTable[Table, Record],
    Record,
    ValueType <: Product with Serializable
  ] = com.outworkers.phantom.udt.columns.UDTColumn[Table, Record, ValueType]

  type UDTListColumn[
    Table <: CassandraTable[Table, Record],
    Record,
    ValueType <: Product with Serializable
  ] = com.outworkers.phantom.udt.columns.UDTListColumn[Table, Record, ValueType]

  type UDTSetColumn[
    Table <: CassandraTable[Table, Record],
    Record,
    ValueType <: Product with Serializable
  ] = com.outworkers.phantom.udt.columns.UDTSetColumn[Table, Record, ValueType]

  type UDTMapKeyColumn[
    Table <: CassandraTable[Table, Record],
    Record,
    KeyType <: Product with Serializable,
    ValueType
  ] = com.outworkers.phantom.udt.columns.UDTMapKeyColumn[Table, Record, KeyType, ValueType]

  type UDTMapValueColumn[
    Table <: CassandraTable[Table, Record],
    Record,
    KeyType,
    ValueType <: Product with Serializable
  ] = com.outworkers.phantom.udt.columns.UDTMapValueColumn[Table, Record, KeyType, ValueType]

  type UDTMapEntryColumn[
    Table <: CassandraTable[Table, Record],
    Record,
    KeyType <: Product with Serializable,
    ValueType <: Product with Serializable
  ] = com.outworkers.phantom.udt.columns.UDTMapEntryColumn[Table, Record, KeyType, ValueType]
}
