/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 06/2017.
 */
package com.outworkers.phantom

import shapeless.HList

package object graph extends AttributeParsers {

  type **[+H, +T <: HList] = shapeless.::[H, T]
  def ** = shapeless.::


  implicit class HListAug[T <: HList](val aug: T) extends AnyVal {
    def *:[A](obj: A): **[A, T] = obj :: aug
  }
}
