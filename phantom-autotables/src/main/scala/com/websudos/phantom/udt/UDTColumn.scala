package com.websudos.phantom.udt

import com.datastax.driver.core.Row

class UDTColumn[UDT](implicit wrapper: UDTType[UDT]) {

  def apply(row: Row): UDT = wrapper.fromRow(row)

}
