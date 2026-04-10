package com.httpService.config

object ConfigLoader {
  def load: AppConfig = AppConfig(
    host = sys.env.getOrElse("HOST", "0.0.0.0"),
    port = sys.env.get("PORT").flatMap(_.toIntOption).getOrElse(8010)
  )
}
