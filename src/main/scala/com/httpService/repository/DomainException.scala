package com.httpService.repository

import com.httpService.domain.Models.DomainError

final case class DomainException(error: DomainError)
    extends RuntimeException(error.toString) {
  override def fillInStackTrace(): Throwable = this
}
