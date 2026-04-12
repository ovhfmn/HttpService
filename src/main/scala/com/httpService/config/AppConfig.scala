package com.httpService.config

final case class AppConfig(
                            host: String,
                            port: Int
                          )

final case class DbConfig(
                           url: String,
                           user: String,
                           password: String
                         )