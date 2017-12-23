/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 06/2017.
 */
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
