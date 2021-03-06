/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 06/2017.
 */
package com.outworkers.phantom.graph

import java.util.Date

import com.datastax.dse.driver.api.core.graph.GraphResultSet
import com.outworkers.phantom.builder.syntax.CQLSyntax
import org.slf4j.LoggerFactory
import shapeless.{HList, Poly1}

trait AttributeParser[T] {
  def schema: String

  def parse(name: String, res: GraphResultSet): T = {
    res.one().asVertex().property(name).asInstanceOf[T]
  }

}

trait AttributeParsers {
  implicit case object IntParser extends AttributeParser[Int] {
    override def schema: String = CQLSyntax.Types.Int
  }

  implicit case object StringParser extends AttributeParser[String] {
    override val schema: String = CQLSyntax.Types.Text
  }

  implicit case object DateParser extends AttributeParser[Date] {
    override val schema: String = CQLSyntax.Types.Date
  }

}

abstract class Attribute[Type : AttributeParser] {
  def queryString: String = implicitly[AttributeParser[Type]].schema

  def name: String = ""
}

private object NameMapper extends Poly1 {
  implicit def caseGeneric[T <: Attribute[T]] = at[T](_.name)
}

private object Extractor extends Poly1 {
  implicit def caseGeneric[T : AttributeParser] = at[((Attribute[T], GraphResultSet))] {
    case (attr, row) => implicitly[AttributeParser[T]].parse(attr.name, row)
  }
}

abstract class GraphNode[
  GraphType <: GraphNode[GraphType, GraphRecord, All],
  GraphRecord,
  All <: HList
] {
  def all: HList

  lazy val logger = LoggerFactory.getLogger(getClass.getName.stripSuffix("$"))

  def tableName: String = ""
}


