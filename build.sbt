import Settings._

lazy val boot = (project in file("boot"))
  .settings(commonSettings)
  .settings(sbtSettings)
  .settings(
    libraryDependencies ++= Seq()
  )

lazy val root =
  (project in file("."))
    .aggregate(boot)
    .settings(sbtSettings)
