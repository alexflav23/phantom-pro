/*
 * Copyright (C) 2012 - 2017 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 6/2017.
 */
package com.outworkers.phantom.migrations

import com.datastax.driver.core.Session
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.query.execution.QueryCollection
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.macros.RootMacro

import scala.concurrent.ExecutionContextExecutor
import scala.reflect.macros.whitebox

trait MigrationHelper[DB <: Database[DB]] {
  def migrations(database: DB)(
    implicit session: Session,
    space: KeySpace,
    ec: ExecutionContextExecutor,
    diffConfig: DiffConfig
  ): QueryCollection[Seq]
}


object MigrationHelper {

  implicit def materialize[DB <: Database[DB]]: MigrationHelper[DB] = macro MigrationHelperMacro.macroImpl[DB]

  def apply[DB <: Database[DB]](implicit ev: MigrationHelper[DB]): MigrationHelper[DB] = ev

}

@macrocompat.bundle
class MigrationHelperMacro(override val c: whitebox.Context) extends RootMacro {

  import c.universe._

  private[this] val queryPkg = q"_root_.com.outworkers.phantom.builder.query"
  private[this] val macroPkg = q"_root_.com.outworkers.phantom.migrations"
  private[this] val sessionTpe = tq"_root_.com.datastax.driver.core.Session"
  private[this] val ecTpe = tq"_root_.scala.concurrent.ExecutionContextExecutor"

  private[this] val diffConfigTpe = tq"$macroPkg.DiffConfig"

  private[this] val keySpaceTpe = tq"_root_.com.outworkers.phantom.connectors.KeySpace"
  private[this] val seqTpe: Tree => Tree = { tpe =>
    tq"_root_.scala.collection.immutable.Seq[$tpe]"
  }

  private[this] val seqCmp = q"_root_.scala.collection.immutable.Seq"
  private[this] val spaceTerm = TermName("space")
  private[this] val prefix = q"_root_.com.outworkers.phantom.database"
  private[this] val tableSymbol = tq"_root_.com.outworkers.phantom.CassandraTable[_, _]"

  def additionTree(input: Seq[Tree]): Tree = {
    input.foldRight(q"new $queryPkg.ExecutableCreateStatementsList[Seq]()") { case (el, acc) =>
      q"$acc ++ $el"
    }
  }

  def macroImpl[T <: Database[T] : WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[T]

    val accessors = filterMembers[CassandraTable[_, _]](tpe, Some(_))
    val dbTerm = TermName("database")

    val tableList = accessors.map(sym => {
      val name = sym.asTerm.name.toTermName
      q"""new $macroPkg.TableMigrations(db.$name)"""
    })

    q"""
       new $macroPkg.MigrationHelper[$tpe] {
         def migrate($dbTerm: $tpe)(
            session: $sessionTpe,
            $spaceTerm: $keySpaceTpe,
            ec: $ecTpe,
            conf: $diffConfigTpe
         ): $queryPkg.ExecutableCreateStatementsList[Seq] = ${additionTree(tableList)}
       }
     """
  }
}