package com.websudos.phantom.udt

import com.datastax.driver.core.Row

import scala.util.Try

abstract class UDTType[T] {

  def fromRow(row: Row): Try[T]

  def name: String

  def asCql(udt: T): String

}
