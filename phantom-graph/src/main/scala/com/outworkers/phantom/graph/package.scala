package com.outworkers.phantom

import shapeless.HList

package object graph extends AttributeParsers {

  type **[+H, +T <: HList] = shapeless.::[H, T]
  def ** = shapeless.::


  implicit class HListAug[T <: HList](val aug: T) extends AnyVal {
    def *:[A](obj: A): **[A, T] = obj :: aug
  }

}
