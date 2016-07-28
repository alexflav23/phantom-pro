def outworkersPattern: Patterns = {
  val pattern = "[organisation]/[module](_[scalaVersion])(_[sbtVersion])/[revision]/[artifact]-[revision](-[classifier]).[ext]"

  Patterns(
    pattern :: Nil,
    pattern :: Nil,
    isMavenCompatible = true
  )
}

resolvers ++= Seq(
  "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "jgit-repo" at "http://download.eclipse.org/jgit/maven",
  "Twitter Repo"                                       at "http://maven.twttr.com/",
  "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/",
  Resolver.bintrayRepo("websudos", "oss-releases"),
  Resolver.url("scoverage-bintray", url("https://dl.bintray.com/sksamuel/sbt-plugins/"))(Resolver.ivyStylePatterns),
  Resolver.bintrayRepo("websudos", "oss-releases"),
  Resolver.bintrayRepo("websudos", "internal-releases"),
  Resolver.url("Websudos OSS", url("http://dl.bintray.com/websudos/oss-releases"))(Resolver.ivyStylePatterns),
  Resolver.url("Websudos Internal", url("http://dl.bintray.com/internal-releases"))(Resolver.ivyStylePatterns)
)

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.5")

addSbtPlugin("com.twitter" %% "scrooge-sbt-plugin" % "3.15.0")

addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "1.3.1")

addSbtPlugin("org.scoverage" %% "sbt-coveralls" % "1.0.0")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.7.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.5")

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

addSbtPlugin("com.websudos" % "sbt-package-dist" % "1.2.0")

addSbtPlugin("com.websudos" %% "phantom-sbt" % "1.27.0")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.10")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.5")