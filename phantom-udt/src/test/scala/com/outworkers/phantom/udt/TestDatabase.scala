package com.outworkers.phantom.udt

import com.outworkers.phantom.builder.query.{CQLQuery, ExecutableStatementList}
import com.outworkers.phantom.builder.serializers.KeySpaceSerializer
import com.outworkers.phantom.dsl.{context => _, _}
import com.outworkers.phantom.udt.tables._

import scala.concurrent.{ExecutionContextExecutor, Future}

class TestDatabase(override val connector: KeySpaceDef) extends Database[TestDatabase](connector) {

  object udtTable extends ConcreteTestTable with connector.Connector

  object nestedUdtTable extends ConcreteNestedUdtsTable with connector.Connector

  object nestedSetUdtTable extends ConcreteNestedUdtSetsTable with connector.Connector

  object nestedMapUdtTable extends ConcreteNestedMapsTable with connector.Connector

  object collectionTable extends ConcreteUDTCollectionsTable with connector.Connector

  def createUdts: ExecutableStatementList  = {
    val queries = tables.toSeq map { tb =>
      val cols = tb.columns.map {
        case u: UDTColumn[_, _, _] => u.primitive.typeDependencies().map(_.qb) :+ u.primitive.schemaQuery()
        case _ => Seq.empty[CQLQuery]
      }
      cols
    }

    val list = queries.flatten.flatten.distinct

    new ExecutableStatementList(list)
  }

  override def createAsync()(implicit ex: ExecutionContextExecutor): Future[Seq[ResultSet]] = {
    createUdts.future() flatMap (_ => super.createAsync())
  }
}

object TestConnector {

  val connector = ContactPoint.local
    .noHeartbeat()
    .withClusterBuilder(_.withoutJMXReporting()
      .withoutMetrics()
    ).keySpace("phantom_pro", (session, keyspace) => {
    KeySpaceSerializer(keyspace).ifNotExists()
      .`with`(replication eqs SimpleStrategy.replication_factor(2))
      .and(durable_writes eqs true).qb.queryString
  })
}

object TestDatabase extends TestDatabase(TestConnector.connector)

