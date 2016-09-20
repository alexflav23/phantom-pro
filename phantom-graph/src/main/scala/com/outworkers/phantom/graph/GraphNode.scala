package com.outworkers.phantom.graph

import java.util.Date

import com.datastax.driver.dse.graph.GraphResultSet
import com.websudos.phantom.builder.syntax.CQLSyntax
import org.slf4j.LoggerFactory
import shapeless.ops.hlist.Mapper
import shapeless.{Generic, HList, Poly1}

import scala.reflect.runtime.{currentMirror => cm, universe => ru}


trait AttributeParser[T] {
  def schema: String

  def parse(name: String, res: GraphResultSet): T = {
    res.one().asVertex().getProperty(name).asInstanceOf[T]
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

  private[this] lazy val _name: String = {
    cm.reflect(this).symbol.name.toTypeName.decodedName.toString
  }

  def name: String = _name
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

  private[this] val instanceMirror = cm.reflect(this)

  protected[phantom] lazy val _name: String = {
    instanceMirror.symbol.name.toTypeName.decodedName.toString
  }

  lazy val logger = LoggerFactory.getLogger(getClass.getName.stripSuffix("$"))

  def tableName: String = _name
}


