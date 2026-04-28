ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.7"

lazy val root = (project in file("."))
  .settings(
    name := "HTTP_service",

    libraryDependencies ++= Seq(
      // core
      "org.typelevel" %% "cats-effect" % "3.5.4",

      // http
      "org.http4s" %% "http4s-ember-server" % "0.23.25",
      "org.http4s" %% "http4s-dsl" % "0.23.25",
      "org.http4s" %% "http4s-circe" % "0.23.25",

      // json
      "io.circe" %% "circe-generic" % "0.14.7",

      // Doobie
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC4",
      "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC4",
      "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC4",

      // Postgres driver
      "org.postgresql" % "postgresql" % "42.7.3",

      // kafka
      "com.github.fd4s" %% "fs2-kafka" % "3.5.1",

      // config
      "com.github.pureconfig" %% "pureconfig-core"        % "0.17.6",
      "com.github.pureconfig" %% "pureconfig-cats-effect" % "0.17.6",

      // logging
      "org.typelevel" %% "log4cats-slf4j" % "2.6.0",
      "ch.qos.logback" % "logback-classic" % "1.5.13",
      "net.logstash.logback" % "logstash-logback-encoder" % "7.4",

      // test
      "org.typelevel" %% "munit-cats-effect" % "2.0.0" % Test,
      "org.http4s" %% "http4s-ember-client" % "0.23.23" % Test,

      // Testcontainers
      "com.dimafeng" %% "testcontainers-scala-munit" % "0.41.4" % Test,
      "com.dimafeng" %% "testcontainers-scala-postgresql" % "0.41.4" % Test
    )
  )

