import Settings._

lazy val boot = (project in file("boot"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq()
  )


lazy val root =
  (project in file("."))
    .aggregate(boot)
