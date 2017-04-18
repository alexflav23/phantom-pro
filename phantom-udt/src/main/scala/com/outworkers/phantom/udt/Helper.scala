package com.outworkers.phantom.udt

import com.outworkers.phantom.builder.QueryBuilder
import scala.collection.JavaConverters._

object Helper {
  def getSet[T](set: java.util.Set[T]): scala.collection.immutable.Set[T] = {
    set.asScala.toSet[T]
  }

  def getList[T](list: java.util.List[T]): scala.collection.immutable.List[T] = {
    list.asScala.toList
  }

  def getMap[K, V](map: java.util.Map[K, V]): scala.collection.immutable.Map[K, V] = {
    map.asScala.toMap[K, V]
  }

  def frozen(str: String): String = {
    QueryBuilder.Collections.diamond("frozen", str).queryString
  }
}
