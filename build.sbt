import Settings._

lazy val boot = (project in file("boot"))
  .settings(commonSettings)
  .settings(
    resolvers += "Maven Repo on github" at "https://BambooTuna.github.io/WebSocketManager/",
    libraryDependencies ++= Seq(
      "com.github.BambooTuna" %% "websocketmanager" % "1.0.2-SNAPSHOT"
    )
  )


lazy val root =
  (project in file("."))
    .aggregate(boot)
