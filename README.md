### phantom-pro

[![Build Status](https://travis-ci.com/outworkers/phantom-pro.svg?token=tyRTmBk14WrDycpepg9c&branch=develop)](https://travis-ci.com/outworkers/phantom-pro) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/84218b943573469dbf2c96034f957526)](https://www.codacy.com?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=outworkers/phantom-pro&amp;utm_campaign=Badge_Grade)

Professional edition of phantom, with support for UDTs, tuples, materalized views, Spark, migrations.

### Getting started

- [ ] Add the `phantom-pro` resolvers and dependencies using [the install guide](docs/install.md)
- [ ] Read the rest of the documentation here.

#### Phantom DSE

#### Phantom Graph

#### Phantom UDT

UDTs or User Defined Types give you the opportunity to lift Scala case classes
into natively supported Cassandra types. All operations are schemaful and unlike
JSON columns for example, the duplication of "keys" is not necessary, meaning it's
a lot more efficient to store custom types as UDTs than anything else.

Cassandra is capable of type-checking UDTs and they are typed even within Cassandra itself,
as every `@Udt` annotation you use on a case class ends up becoming a `CREATE TYPE` in Cassandra.

#### Phantom Spark
