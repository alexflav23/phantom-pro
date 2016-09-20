package com.outworkers.phantom.graph

import shapeless.HNil

case class Record(
  id: Int,
  text: String,
  prop: (String, String)
)


class SampleTable extends GraphNode[SampleTable, Record, HNil] {

  object id extends Attribute[Int]
  object text extends Attribute[String]

  def all = id *: text *: HNil
}
