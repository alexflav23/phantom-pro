package com.websudos.phantom.udt

import org.scalatest.FlatSpec
import com.websudos.phantom.dsl._

class UdtTest extends FlatSpec {

  @Udt
  case class Testclass(id: Int, name: String)

  it should "deserialize row" in {
    val value = Testclass(1, "abc")
    Testclass.fromRow(null)
  }

}
