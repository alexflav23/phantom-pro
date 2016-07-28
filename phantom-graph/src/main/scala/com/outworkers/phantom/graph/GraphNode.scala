package com.outworkers.phantom.graph

import com.websudos.diesel.engine.reflection.EarlyInit


trait GraphAttribute[Type] {
  def queryString: String
}

class GraphNode[GraphType <: GraphNode[GraphType, GraphRecord], GraphRecord] extends EarlyInit[GraphAttribute[_]] {
  def attributes: Seq[GraphAttribute[_]] = initialize()



}
