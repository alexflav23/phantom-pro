package com.outworkers.phantom.udt

package object debug {

  object optionTypes {
    sealed trait ShowTrees
  }

  import optionTypes._

  object options {

    /** Import this value to have Iota print the macro generated code
      * to the console during compilation
      */
    implicit val ShowTrees: ShowTrees = null.asInstanceOf[ShowTrees]
  }
}
