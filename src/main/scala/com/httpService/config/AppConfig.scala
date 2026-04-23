package com.httpService.config

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

final case class AppConfig(
    server: ServerConfig,
    db: DbConfig
) derives ConfigReader

final case class ServerConfig(
    host: String,
    port: Int
) derives ConfigReader

final case class DbConfig(
    url: String,
    user: String,
    password: String,
    driver: String
) derives ConfigReader
