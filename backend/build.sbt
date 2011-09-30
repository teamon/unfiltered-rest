organization := "com.monterail"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
    "net.databinder" %% "unfiltered-netty" % "0.4.1",
    "net.databinder" %% "unfiltered-json" % "0.4.1",
    "net.liftweb" %% "lift-json-scalaz" % "2.4-M4"
)
