package com.websudos.phantom.enterprise.builder

import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.syntax.CQLSyntax
import com.websudos.phantom.connectors.KeySpace

private object Strategies {
  final val NetworkTopologyStrategy = "NetworkTopologyStrategy"
  final val SimpleStrategy = "SimpleStrategy"
}

sealed abstract class ReplicationStrategy(val name: String) {

  private[this] def rootStrategy(strategy: String) = {
    CQLQuery(CQLSyntax.Symbols.`{`).forcePad
      .appendSingleQuote(CQLSyntax.CompactionOptions.`class`)
      .forcePad.append(":")
      .forcePad.appendSingleQuote(strategy)
  }
}

object NetworkTopologyStrategy extends ReplicationStrategy(Strategies.NetworkTopologyStrategy)
object SimpleStrategy extends ReplicationStrategy(Strategies.SimpleStrategy)

sealed class KeySpaceSerializer(val keySpace: KeySpace, val qb: CQLQuery = CQLQuery.empty) {

  def strategy(strategy: ReplicationStrategy): KeySpaceSerializer = {
    new KeySpaceSerializer(keySpace, qb.append(s"'class': ${strategy.name}" ))
  }

  def durable_writes(flag: Boolean): KeySpaceSerializer = {
    new KeySpaceSerializer(keySpace, qb.pad.append(s"AND DURABLE_WRITES $flag"))
  }
}

class RootSerializer(val keySpace: KeySpace) {
  def ifNotExists(): KeySpaceSerializer = {
    new KeySpaceSerializer(keySpace, CQLQuery(s"CREATE KEYSPACE IF NOT EXISTS ${keySpace.name}"))
  }

  protected[phantom] def default(): KeySpaceSerializer = {
    new KeySpaceSerializer(keySpace, CQLQuery(s"CREATE KEYSPACE ${keySpace.name}"))
  }
}

object KeySpaceSerializer {

  def apply(name: String) = new RootSerializer(KeySpace(name))

  def apply(keySpace: KeySpace) = new RootSerializer(keySpace)

}

object Builder {

  implicit def rootSerializerToKeySpaceSerializer(serializer: RootSerializer): KeySpaceSerializer = {
    serializer.default()
  }

  def createKeyspace(name: String): RootSerializer = new RootSerializer(KeySpace(name))
}
