package com.httpService.root

import cats.effect.{IO, IOApp}
import com.comcast.ip4s.{Host, Port, host, port}
import com.httpService.app.AppBuilder
import org.http4s.ember.server.EmberServerBuilder

/**
 * Delegates assembly to [[AppBuilder]], then blocks on the Ember server.
 * All resources are released on JVM shutdown.
 */
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
          .withHost(host)
          .withPort(port)
          .withHttpApp(httpApp)
          .build
          .useForever
      } yield ()
    }
}
