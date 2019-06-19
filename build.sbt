name := """TB"""

version := "0.1"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.12.8"

javacOptions ++= Seq(
  "-source", "1.8",
  "-target", "1.8",
  "-encoding", "UTF-8",
  "-parameters",
  "-Xlint:unchecked",
  "-Xlint:deprecation",
  "-Werror",
)

//crossScalaVersions := Seq("2.11.12", "2.12.8")

libraryDependencies += guice

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  javaJpa,
  "org.hibernate" % "hibernate-entitymanager" % "5.4.0.Final",
  "org.postgresql" % "postgresql" % "9.4.1208.jre7",
  javaWs,
  "com.adrianhurt" %% "play-bootstrap" % "1.4-P26-B4-SNAPSHOT",
  "org.mockito" % "mockito-core" % "2.10.0" % "test"
)

// Testing libraries for dealing with CompletionStage...
libraryDependencies += "org.assertj" % "assertj-core" % "3.11.1" % Test
libraryDependencies += "org.awaitility" % "awaitility" % "3.1.3" % Test
libraryDependencies += javaJdbc % Test

libraryDependencies += "org.quartz-scheduler" % "quartz" % "2.3.1"

// Make verbose tests
testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v"))
