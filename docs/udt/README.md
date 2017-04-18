### phantom-udt

The phantom UDT module adds support for User Defined Types. It allows you
to efficiently lift Scala case classes into Cassandra data types. They work
with any Cassandra version newer than 2.2.

### Using UDTs

### UDT columns

There are a number of columns currently available to use with UDT types. In the below
table, we are going to exemplify available column types based on the following
Scala case class.

```scala

import com.outworkers.phantom.udt._

@Udt case class Record(
  id: UUID,
  name: String,
  text: String,
  age: Int
)
```

The name of the UDT type that will get created in Cassandra will be the name
of the case class defined in Scala. Depending on the implicit `NamingStrategy` used
in scope, phantom will alter the name of the `case class` above to `record`, or
it will use snake or camel casing as the strategy dictates in the event where
the name is made by multiple words.

| Column | Scala Type | Cassandra type |
| ====== | ==== | ============== |
| UDTColumn[Record]     | Record       | Record
| UDTListColumn[Record] | List[Record] | list<frozen<Record>>            |
| UDTSetColumn[Record]  | Set[Record]  | set<frozen<Record>>             |
| UDTMapKeyColumn[Record, V]  | Map[Record, V]  | map<frozen<Record, V>> |
| UDTMapValueColumn[V, Record]  | Map[V, Record]  | map<frozen<V, Record>> |
| UDTMapEntryColumn[Record, Record]  | Map[Record, Record]  | map<frozen<Record, Record>> |


### Creating the schema for UDTs

At this point in time, phantom-pro does not offer automated initilization of UDT
types as part of the `Database.create` execution chain, but it does however offer
tooling required to auto-generate the UDT schema.

In our own tests, you may want to have a look at [TestDatabase.scala](./phantom-udt/test/scala/com/outworkers/phantom/udt/TestDatabase.scala#25)
where we inspect our UDT hierarchy and manually initiaze the the types in the correct order.

This will be automated in a future release. For now, using `ExecutableStatementList` allows
you to execute queries against the database in sequence. The below code snippet
demonstrates a potential UDT hierarchy and how to initialize it.

In this example, we override the default `createAsync` method of a database
and make sure to initialize UDT types before the Cassandra table definitions
in the database. In this example, we rely on the `sequentialFuture` method
of `ExecutableStatementList` to execute schema queries one at a time in the
order they were defined.

```scala

class TestDatabase(
  override val connector: CassandraConnection
) extends Database[TestDatabase](connector) {

  def initUds: ExecutableStatementList[Seq] = {
    new ExecutableStatementList[Seq](
      Seq(
        UDTPrimitive[Location].schemaQuery(),
        UDTPrimitive[Address].schemaQuery(),
        UDTPrimitive[CollectionUdt].schemaQuery(),
        UDTPrimitive[NestedRecord].schemaQuery(),
        UDTPrimitive[NestedMaps].schemaQuery(),
        UDTPrimitive[CollectionSetUdt].schemaQuery(),
        UDTPrimitive[NestedSetRecord].schemaQuery(),
        UDTPrimitive[Test].schemaQuery(),
        UDTPrimitive[Test2].schemaQuery(),
        UDTPrimitive[ListCollectionUdt].schemaQuery()
      )
    )
  }
}

override def createAsync()(implicit ex: ExecutionContextExecutor): Future[Seq[ResultSet]] = {
  initUds.sequentialFuture() flatMap(_ => super.createAsync())
}
```
