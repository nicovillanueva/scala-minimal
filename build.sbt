// import org.joda.time.DateTime
import ReleaseTransformations._
import sbt.Keys._

name := """minimal-scala"""

version := "1.0"

scalaVersion := "2.11.7"

lazy val root = (project in file("."))
  .enablePlugins(SonarRunnerPlugin)

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runTest,
  // setReleaseVersion,
  // commitReleaseVersion,
  // tagRelease,
  publishArtifacts,
  // setNextVersion,
  commitNextVersion
  // pushChanges
)

sonarProperties ++= Map(
  "sonar.host.url" -> "http://devsonarqube.marathon.l4lb.thisdcos.directory:9000",
  "sonar.sources" -> "src/main",
  "sonar.tests" -> "src/test",
  "sonar.scoverage.reportPath" -> "target/scala-2.11/scoverage-report/scoverage.xml",
  "sonar.exclusions" -> "*.java",
  "sonar.coverage.exclusions" -> "*.java",
  "sonar.java.binaries" -> "target/scala-2.11/classes",
  "sonar.sourceEncoding" -> "utf-8",
  "commons.sonar.sources" -> "app",
  "sonar.webhooks.global" -> "http://jenkins.marathon.l4lb.thisdcos.directory:10101/sonarqube-webhook/"
)
sonarRunnerOptions := Seq("-e", "-X")
coverageEnabled := true

fork in run := true
