package com.outworkers.phantom.udt

import com.websudos.phantom.builder.query.ExecutableStatementList
import com.websudos.phantom.dsl.{Database, KeySpaceDef, ResultSet, ContactPoint}

import scala.concurrent.{ExecutionContextExecutor, Future}

class TestDatabase(override val connector: KeySpaceDef) extends Database(connector) {

  object udtTable extends ConcreteTestTable with connector.Connector

  def createUdts: ExecutableStatementList  = {
    val queries = tables flatMap { _.columns collect { case c: UDTColumn[_, _, _] => c.create.qb } }
    new ExecutableStatementList(queries.toSeq)
  }

  override def createAsync()(implicit ex: ExecutionContextExecutor): Future[Seq[ResultSet]] = {
    createUdts.future() flatMap (_ => super.createAsync())
  }
}

object TestConnector {
  val connector = ContactPoint.local.keySpace("phantom_pro")
}

object TestDatabase extends TestDatabase(TestConnector.connector)

