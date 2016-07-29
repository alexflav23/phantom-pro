package com.outworkers.phantom.udt

import com.websudos.phantom.builder.query.ExecutableStatementList
import com.websudos.phantom.dsl._

import scala.concurrent.Future

class TestDatabase(override val connector: KeySpaceDef) extends Database(connector) {

  object udtTable extends ConcreteTestTable with connector.Connector

  def createUdts: Future[Seq[ResultSet]]  = {
    val queries = tables flatMap { _.columns collect { case c: UDTColumn[_, _, _] => c.create.qb } }
    Console.println(s"${queries.size} udt queries to execute.")
    Console.println(queries.map(_.queryString).mkString("\n"))
    new ExecutableStatementList(queries.toSeq).future()
  }
}

object TestConnector {
  val connector = ContactPoint.local.keySpace("phantom_pro")
}

object TestDatabase extends TestDatabase(TestConnector.connector)

