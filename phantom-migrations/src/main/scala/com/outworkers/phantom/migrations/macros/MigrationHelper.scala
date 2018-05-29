/*
 * Copyright (C) 2012 - 2018 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 6/2017.
 */
package com.outworkers.phantom.migrations.macros

import com.datastax.driver.core.Session
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.query.execution.QueryCollection
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.macros.RootMacro
import com.outworkers.phantom.macros.toolbelt.WhiteboxToolbelt
import com.outworkers.phantom.migrations.diffs.DiffConfig

import scala.concurrent.ExecutionContextExecutor
import scala.reflect.macros.whitebox

@deprecated("Not working as expected", "0.20.0")
trait MigrationHelper[DB <: Database[DB]] {
  def migrations(database: DB)(
    implicit session: Session,
    space: KeySpace,
    ec: ExecutionContextExecutor,
    diffConfig: DiffConfig
  ): QueryCollection[Seq]
}

object MigrationHelper {
  val cache = new WhiteboxToolbelt.Cache

  implicit def materialize[DB <: Database[DB]]: MigrationHelper[DB] = macro MigrationHelperMacro.materialize[DB]

  def apply[DB <: Database[DB]](implicit ev: MigrationHelper[DB]): MigrationHelper[DB] = ev

}

@macrocompat.bundle
class MigrationHelperMacro(override val c: whitebox.Context) extends RootMacro {

  import c.universe._

  private[this] val migrationsPkg = q"_root_.com.outworkers.phantom.migrations"
  private[this] val sessionTpe = typeOf[_root_.com.datastax.driver.core.Session]
  private[this] val ecTpe = typeOf[_root_.scala.concurrent.ExecutionContextExecutor]

  private[this] val diffConfigTpe = tq"$migrationsPkg.DiffConfig"

  private[this] val keySpaceTpe = typeOf[_root_.com.outworkers.phantom.connectors.KeySpace]

  private[this] val spaceTerm = TermName("space")
  private[this] val queryCol = typeOf[_root_.com.outworkers.phantom.builder.query.execution.QueryCollection[Seq]]
  private[this] val executableQuery = typeOf[_root_.com.outworkers.phantom.builder.query.execution.ExecutableCqlQuery]

  val confTerm = TermName("conf")

  def additionTree(input: Seq[Tree]): Tree = {
    input.foldRight(q"new $queryCol(_root_.scala.collection.Seq.empty[$executableQuery])") { case (el, acc) =>
      q"$acc ++ $el"
    }
  }

  def materialize[T <: Database[T] : WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[T]

    memoize[Type, Tree](MigrationHelper.cache)(tpe, macroImpl)
  }

  def macroImpl(tpe: Type): Tree = {
    val accessors = filterMembers[CassandraTable[_, _]](tpe, Some(_))
    val dbTerm = TermName("db")

    val tableList = accessors.map(sym => {
      val name = sym.asTerm.name.toTermName
      q"""new $migrationsPkg.TableMigrations($dbTerm.$name).automigrate($confTerm)"""
    })

   val tree = q"""
       new $migrationsPkg.MigrationHelper[$tpe] {
         def migrations($dbTerm: $tpe)(
            implicit session: $sessionTpe,
            $spaceTerm: $keySpaceTpe,
            ec: $ecTpe,
            $confTerm: $diffConfigTpe
         ): $queryCol = ${additionTree(tableList)}
       }
     """
    println(showCode(tree))
    tree
  }
}