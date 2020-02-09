import sbt.Keys._
import sbt._
import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport._

object Settings {

  val sdk8 = "adoptopenjdk/openjdk8:x86_64-ubuntu-jdk8u212-b03-slim"
  val sdk11 = "adoptopenjdk/openjdk11:x86_64-alpine-jdk-11.0.4_11-slim"

  lazy val commonSettings = Seq(
    libraryDependencies ++= Seq(
      Circe.core,
      Circe.generic,
      Circe.parser,
      Circe.shapes,
      Enumeratum.version,
      Akka.http,
      Akka.stream,
      Akka.slf4j,
      Akka.`akka-http-crice`,
      Logback.classic,
      LogstashLogbackEncoder.encoder,
      Config.core,
      Monix.version
    ),
    scalafmtOnCompile in Compile := true,
    scalafmtTestOnCompile in Compile := true
  )

  lazy val sbtSettings = Seq(
    organization := "com.github.BambooTuna",
    scalaVersion := "2.12.8",
    version := "1.0.0-SNAPSHOT",
    name := "CryptoExchangeAPI",
    publishTo := Some(Resolver.file("CryptoExchangeAPI",file("."))(Patterns(true, Resolver.mavenStyleBasePattern)))
  )

}
