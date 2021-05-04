resolvers ++= Seq(
  "jgit-repo" at "https://download.eclipse.org/jgit/maven",
  "Twitter Repo" at "https://maven.twttr.com/",
  Resolver.sonatypeRepo("releases"),
  Resolver.bintrayIvyRepo("sksamuel", "sbt-plugins"),
  Resolver.bintrayIvyRepo("twittercsl-ivy", "sbt-plugins"),
  Resolver.bintrayRepo("twittercsl", "sbt-plugins")
)


addSbtPlugin("com.codecommit" % "sbt-github-packages" % "0.5.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")

addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.6")

addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "1.6.0")

addSbtPlugin("org.scoverage" %% "sbt-coveralls" % "1.2.7")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.5")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.0")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.5.0")

addSbtPlugin("com.twitter" % "scrooge-sbt-plugin" % "19.10.0")

addSbtPlugin("org.tpolecat" % "tut-plugin" % "0.6.13")

libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.22"

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.12")
