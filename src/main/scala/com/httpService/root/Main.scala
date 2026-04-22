package com.httpService.root

import cats.effect.{IO, IOApp}
import com.comcast.ip4s.{host, port}
import com.httpService.app.AppBuilder
import org.http4s.ember.server.EmberServerBuilder

object Main extends IOApp.Simple {
  
  val program: IO[Unit] =
    AppBuilder.build.use { httpApp =>
      EmberServerBuilder
        .default[IO]
        .withHost(host"0.0.0.0")
        .withPort(port"8010")
        .withHttpApp(httpApp)
        .build
        .useForever
    }

  override def run: IO[Unit] = program

}