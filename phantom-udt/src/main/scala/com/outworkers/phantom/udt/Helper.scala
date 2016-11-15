package com.outworkers.phantom.udt

import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.query.CQLQuery

import scala.collection.JavaConversions

object Helper {
  def getSet[T](set: java.util.Set[T]): scala.collection.immutable.Set[T] = {
    JavaConversions.asScalaSet(set).toSet[T]
  }

  def getList[T](list: java.util.List[T]): scala.collection.immutable.List[T] = {
    JavaConversions.asScalaBuffer(list).toList
  }

  def getMap[K, V](map: java.util.Map[K, V]): scala.collection.immutable.Map[K, V] = {
    JavaConversions.mapAsScalaMap(map).toMap[K, V]
  }

  def frozen(str: String): String = {
    QueryBuilder.Collections.diamond("frozen", str).queryString
  }
}
