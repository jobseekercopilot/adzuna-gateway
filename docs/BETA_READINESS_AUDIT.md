# Adzuna Gateway beta-readiness audit

## Blocking findings

- **P0 credential response:** non-empty Adzuna identifiers were present in
  current source. They have been removed from this candidate, but must be
  revoked/rotated and the migration history must be scanned before publication.
- **P0 provider compliance:** beta display, caching, quotas, deletion, salary
  attribution, and “Jobs by Adzuna” obligations have not been implemented or
  approved against the organisation's actual account.
- **P0 reproducibility:** the System Data client is a `systemPath` JAR under
  excluded `libs`.
- **P1 correctness:** missing credentials return a successful empty result,
  hiding configuration failure.
- **P1 API safety:** the request is not validated despite validation support.
- **P1 resilience:** a blocking WebClient call has no explicit timeout,
  deadline, rate limiter, circuit breaker, or controlled retry policy.
- **P1 coverage:** no provider mapping, error, fixture, empty-result, or
  contract tests were found.
- **P1 container:** the Dockerfile copies a prebuilt JAR, runs as root, uses an
  unpinned base image, and has no health check.

## Provider evidence

The audit used Adzuna's official
[API terms](https://developer.adzuna.com/docs/terms_of_service) and
[developer overview](https://developer.adzuna.com/overview). Account-specific
written approval remains an external prerequisite; repository code cannot
establish redistribution rights.

## Evidence required to close

Clean-clone build/container evidence; a versioned contract and drift check;
credential rotation plus full-history scan; bounded validation; deadline,
quota, retry and failure tests; mapping fixtures covering missing fields,
salary units, currencies, dates and source URLs; and product-level attribution,
retention, cache and deletion acceptance evidence.

This audit is not a beta-readiness approval.
