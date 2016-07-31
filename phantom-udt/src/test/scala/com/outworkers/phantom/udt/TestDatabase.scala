package com.outworkers.phantom.udt

import com.websudos.phantom.dsl._

import scala.concurrent.Future

class TestDatabase(override val connector: KeySpaceDef) extends Database(connector) {

  object udtTable extends ConcreteTestTable with connector.Connector

  /*
  def createUdts: Future[Seq[ResultSet]]  = {
    val queries = tables flatMap { _.columns collect { case c: UDTColumn[_, _, _] => c.create.qb } }
    new ExecutableStatementList(queries.toSeq).future()
  }*/

  def createUdts: Future[List[ResultSet]] = {
    Future.sequence(
      List(
        udtTable.udt.create().future()
      )
    )
  }
}

object TestConnector {
  val connector = ContactPoint.local.keySpace("phantom_pro")
}

object TestDatabase extends TestDatabase(TestConnector.connector)

