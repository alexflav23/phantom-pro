package com.outworkers.phantom.udt

import com.outworkers.phantom.builder.QueryBuilder

object Helper {
  def frozen(str: String): String = {
    QueryBuilder.Collections.diamond("frozen", str).queryString
  }
}
