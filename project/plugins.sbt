addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.0")
addSbtPlugin("com.geirsson" % "sbt-ci-release" % "1.2.6")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.4.1")

resolvers ++= Seq(
  Resolver.sonatypeRepo("public"),
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases"),
  "Maven repo1" at "http://repo1.maven.org/",
  "Maven repo2" at "http://mvnrepository.com/artifact"
)