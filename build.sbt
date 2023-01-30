import xerial.sbt.Sonatype.GitHubHosting

ThisBuild / organization := "org.jetbrains"

ThisBuild / description :=
  """Nailgun is a client, protocol and server for running Java programs
    |from the command line without incurring the JVM startup overhead.
    |Programs run in the server (which is implemented in Java), and are
    |triggered by the client (C and Python clients available), which
    |handles all I/O.
    |
    |This project contains the server and examples.
    |""".stripMargin.trim

ThisBuild / homepage := Some(url("https://github.com/JetBrains/nailgun"))

ThisBuild / licenses := Seq("Apache-2.0" -> new URL("https://www.apache.org/licenses/LICENSE-2.0"))

ThisBuild / crossPaths := false

ThisBuild / autoScalaLibrary := false

ThisBuild / developers := List(
  Developer("facebook", "Facebook", "", url("https://github.com/facebook")),
  Developer("jetbrains", "JetBrains", "", url("https://github.com/JetBrains"))
)

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/JetBrains/nailgun"),
    "scm:git:git@github.com:JetBrains/nailgun.git",
    "scm:git:git@github.com:JetBrains/nailgun.git"
  )
)

ThisBuild / sonatypeProjectHosting := Some(GitHubHosting("JetBrains", "nailgun", "scala-developers@jetbrains.com"))

lazy val nailgun = project.in(file("."))
  .settings(publish / skip := true)
  .aggregate(`nailgun-server-for-scala-plugin`)

lazy val `nailgun-server-for-scala-plugin` = project.in(file("nailgun-server"))
  .settings(
    name := "nailgun-server-for-scala-plugin",
    javacOptions ++= Seq("--release", "8"),
    libraryDependencies ++= Seq(
      "net.java.dev.jna" % "jna" % "5.12.1" % Provided,
      "net.java.dev.jna" % "jna-platform" % "5.12.1" % Provided,
      "org.mockito" % "mockito-junit-jupiter" % "4.11.0" % Test,
      "net.aichler" % "jupiter-interface" % JupiterKeys.jupiterVersion.value % Test
    )
  )
