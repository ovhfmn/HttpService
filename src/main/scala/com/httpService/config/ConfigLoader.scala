package com.httpService.config

import pureconfig.*
import cats.effect.IO
import pureconfig.generic.derivation.default.*
import pureconfig.generic.*
import scala.deriving.Mirror

object ConfigLoader {
  def load: IO[AppConfig] =
    IO.blocking {
      ConfigSource.default.at("app").loadOrThrow[AppConfig]
    }
}
