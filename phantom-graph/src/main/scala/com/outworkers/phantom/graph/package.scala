package com.outworkers.phantom

import shapeless.HList

package object graph extends AttributeParsers {

  type **[+H, +T <: HList] = shapeless.::[H, T]
  def ** = shapeless.::

}
