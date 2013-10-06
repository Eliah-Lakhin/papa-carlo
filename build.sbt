name := "Papa Carlo"

version := "0.2.0"

scalaVersion := "2.10.0"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0.M6-SNAP8" % "test"

libraryDependencies += "net.liftweb" % "lift-json_2.10" % "2.5-RC6" % "test"

parallelExecution in Test := false

testOptions in Test += Tests.Argument("-oD")
