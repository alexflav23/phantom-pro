package com.websudos.phantom.udt

import com.datastax.driver.core.Row

abstract class UDTType[T] {

  def fromRow(row: Row): T

}
