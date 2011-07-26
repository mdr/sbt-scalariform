
sbtPlugin := true

organization := "com.typesafe"

name := "sbt-scalariform"

version := "0.1"

libraryDependencies += "org.scalariform" %% "scalariform" % "0.1.0"

publishMavenStyle := true

publishTo := Some("Typesafe Publish Repo" at "http://repo.typesafe.com/typesafe/maven-releases/")

credentials += Credentials(Path.userHome / ".ivy2" / "typesafe-credentials")
