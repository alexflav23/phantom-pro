/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 21/11/2017.
 */
package com.outworkers.phantom.dse.mv

import com.outworkers.phantom.builder.query.engine.CQLQuery


class MaterializedViewBuilder {

  def view(name: String, selectQuery: CQLQuery): CQLQuery = {
    CQLQuery(s"CREATE VIEW as $name")
  }
}

class QueryBuilderExtension {



}
