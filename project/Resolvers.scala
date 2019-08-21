import sbt.{Resolver, _}
import sbt.librarymanagement.MavenRepository

/**
  * Advxml
  * Created by geirolad on 26/07/2019.
  *
  * @author geirolad
  */
object Resolvers {

  lazy val all: Seq[MavenRepository] = Seq(
    Resolver.sonatypeRepo("public"),
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases"),
    "Maven repo1" at "http://repo1.maven.org/",
    "Maven repo2" at "http://mvnrepository.com/artifact"
  )
}
