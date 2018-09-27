resolvers ++= Seq(
  "Twitter Repo" at "http://maven.twttr.com/",
  Resolver.jcenterRepo,
  Resolver.typesafeRepo("releases"),
  Resolver.sonatypeRepo("releases")
)

addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "1.5.0")

addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.1.0")

addSbtPlugin("com.codacy" % "sbt-codacy-coverage" % "1.3.0")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.7.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.5")

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

addSbtPlugin("com.eed3si9n" % "sbt-doge" % "0.1.5")

addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.3.7")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.5")

addSbtPlugin("org.tpolecat" % "tut-plugin" % "0.5.2")

addSbtPlugin("org.lyranthe.sbt" % "partial-unification" % "1.1.0")
