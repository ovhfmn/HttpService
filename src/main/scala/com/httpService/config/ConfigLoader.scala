package com.httpService.config

import cats.effect.IO
import pureconfig.ConfigSource

/** 
 * Reads from the `"app"` key in `application.conf` 
 */
object ConfigLoader {
  def load: IO[AppConfig] =
    IO.blocking {
      ConfigSource.default.at("app").loadOrThrow[AppConfig]
    }
}
