/*
 * Copyright (C) 2012 - 2017 Outworkers, Limited. All rights reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * The contents of this file are proprietary and strictly confidential.
 * Written by Flavian Alexandru<flavian@outworkers.com>, 6/2017.
 */
package com.outworkers.phantom.migrations

import com.outworkers.phantom.database.Database
import com.outworkers.phantom.dsl.KeySpace

trait Spec
trait Specified extends Spec
trait Unspecified extends Spec

object Plans {
  sealed trait PlanType
  trait SameKeyspace extends PlanType
  trait TargetKeyspace extends PlanType
}

class MigrationPlan[
  DB <: Database[DB],
  Plan <: Plans.PlanType
](val db: DB, keySpace: KeySpace, conf: DiffConfig)

class MigrationPlanBuilder[
  DB <: Database[DB],
  Plan <: Plans.PlanType,
  HasConf <: Spec,
  HasDB <: Spec,
  HasPlan <: Spec
](val db: Option[DB], keySpace: Option[KeySpace], conf: Option[DiffConfig]) {

  def withDB[DBO <: Database[DBO]](db: DBO)(
    implicit ev: HasDB =:= Unspecified
  ): MigrationPlanBuilder[DBO, Plan, HasConf, HasDB, HasPlan] = {
    new MigrationPlanBuilder[DBO, Plan, HasConf, HasDB, HasPlan](Some(db), keySpace, conf)
  }

  def withConfig(conf: DiffConfig)(
    implicit ev: HasConf =:= Unspecified
  ): MigrationPlanBuilder[DB, Plan, HasConf, HasDB, HasPlan] = {
    new MigrationPlanBuilder(db, keySpace, Some(conf))
  }

  def build()(
    implicit ev: HasConf =:= Specified,
    ev2: HasDB =:= Specified,
    ev3: HasPlan =:= Specified
  ): MigrationPlan[DB, Plan] = new MigrationPlan(db.get, keySpace.get, conf.get)
}

object MigrationPlan {

  def apply[DBO <: Database[DBO]](
    db: DBO
  ): MigrationPlanBuilder[DBO, Plans.SameKeyspace, Unspecified, Specified, Specified] = {
    new MigrationPlanBuilder(Some(db), Some(db.space), None)
  }

  def apply[DBO <: Database[DBO]](
    db: DBO,
    space: KeySpace
  ): MigrationPlanBuilder[DBO, Plans.TargetKeyspace, Unspecified, Specified, Specified] = {
    new MigrationPlanBuilder(Some(db), Some(space), None)
  }
}