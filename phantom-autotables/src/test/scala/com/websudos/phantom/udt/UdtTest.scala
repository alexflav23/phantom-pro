package com.websudos.phantom.udt

import org.scalatest.{FlatSpec, Matchers}
import com.websudos.phantom.dsl._
import org.scalamock.proxy.ProxyMockFactory

class UdtTest extends FlatSpec with Matchers {

  @Udt
  case class Test(id: Int, name: String)

  it should "deserialize row" in {
    val result: Test = Test(1, "hello").fromRow
    result.id shouldBe 666

    println(TestTable.abc)
    println(TestTable.name)
  }

}
