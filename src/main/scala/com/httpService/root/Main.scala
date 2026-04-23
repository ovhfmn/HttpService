package com.httpService.root

import cats.effect.{IO, IOApp}
import com.comcast.ip4s.{host, port}
import com.httpService.app.AppBuilder
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s.*

object Main extends IOApp.Simple {

  val run: IO[Unit] =
    AppBuilder.build.use { case (httpApp, config) =>
      for {
        host <- IO.fromOption(Host.fromString(config.server.host))(
          new IllegalArgumentException(s"Invalid host: ${config.server.host}"))
        port <- IO.fromOption(Port.fromInt(config.server.port))(
          new IllegalArgumentException(s"Invalid port: ${config.server.port}"))

        _ <- EmberServerBuilder
          .default[IO]
          .withHost(host"0.0.0.0")
          .withPort(port"8010")
          .withHttpApp(httpApp)
          .build
          .useForever
      } yield ()
    }
}
