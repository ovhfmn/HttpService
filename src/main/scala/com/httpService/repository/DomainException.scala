package com.httpService.repository

import com.httpService.domain.Models.DomainError

/**
 * Carries a [[DomainError]] across exception-based control flow boundaries
 * (e.g. Doobie's `ConnectionIO.raiseError` / `.attempt`).
 *
 * This is NOT a domain error itself — it's infrastructure plumbing for
 * propagating domain failures through APIs that require `Throwable`.
 * Domain logic should never construct or pattern-match this directly;
 * only persistence transaction code raises it, and only the service layer
 * (via `.attempt`) unwraps it.
 */
final case class DomainException(error: DomainError)
    extends RuntimeException(error.toString) {
  /**
   * fillInStackTrace override is a real production optimization — these
   * aren't "real" exceptions representing bugs, they're control-flow
   * signals for expected domain failures (insufficient funds, not found, etc.).
   * JVM stack trace capture is expensive; for high-throughput control-flow exceptions.
   */
  override def fillInStackTrace(): Throwable = this
}
