# Adzuna credential operations

## Security boundary

Adzuna Gateway has no live credential in source, examples, images or fixture
configuration. It starts in `FIXTURE` mode by default. A production profile
rejects fixture mode, and enabled `LIVE` mode requires both
`ADZUNA_APP_ID` and `ADZUNA_APP_KEY` before startup completes.

`ADZUNA_ENABLED=false` is the emergency kill switch. It prevents provider
requests and returns an empty provider result. It is not evidence that an
exposed credential has been revoked.

Live values must be injected from the approved deployment secret store into
the two environment variables. Do not place either value in Git, GitHub,
Compose files, issue comments, pull requests, build arguments, container
images, shell history, support tickets, dashboards or log configuration.

## Initial exposure response

The repository cannot revoke or rotate an Adzuna account credential. The
provider account administrator must:

1. Disable Adzuna traffic with `ADZUNA_ENABLED=false`.
2. Revoke the previously exposed application ID/key pair in the provider
   account before relying on source cleanup.
3. Create a replacement pair under the approved organisation account and
   least-privilege product/usage settings.
4. Store the replacement only in the approved secret manager.
5. Update the deployment's secret references without copying values into an
   issue, command line or deployment manifest.
6. Start one approved environment with `EXTERNAL_PROVIDER_MODE=LIVE` and both
   injected variables, then verify health and a bounded synthetic request.
7. Confirm the old pair is rejected and the replacement pair is not visible
   in application, platform, proxy or provider-support logs.
8. Re-enable traffic only after account ownership, terms, attribution, quota,
   retention and cost approvals are recorded.

Keep the following evidence in the restricted security record, not in this
repository:

- rotation UTC date/time and named account administrator;
- provider account/application reference without either credential value;
- revocation and replacement confirmation;
- secret-manager reference/version without secret content;
- affected environments and deployment revision;
- redacted verification result; and
- incident owner, review outcome and next renewal date.

## Routine renewal

Rotate on the organisation schedule and immediately after suspected exposure,
provider-account ownership changes or access-control incidents. Use an
overlap only if the provider supports two independently revocable keys and the
security owner approves it. Revoke the former key after the replacement is
healthy, then remove its secret version according to the secret-manager
retention policy.

## Logging and support

Adzuna credentials are query parameters required by the provider API. Never log
the request URI, `WebClientResponseException`, request object, Reactor Netty
wiretap or HTTP-client debug output in LIVE mode. Application provider failures
log only fixed operation text, HTTP status, duration and exception class.

Do not paste an exception, request URL or provider dashboard screenshot into a
ticket until both credential parameters and account identifiers have been
removed. If a credential reaches a log or support channel, disable traffic,
revoke it and follow the incident steps above.

## Repository and history verification

Use the repository-owned fail-closed controls from a complete authenticated
clone:

```bash
./scripts/test-secret-history.sh
./scripts/verify-secret-history.sh
```

The verification refuses shallow or empty history and scans all reachable
commits. Keep scanner version, complete-history proof, commit SHA, UTC time and
pass/fail result. Do not retain a matching secret value in the evidence.

Also verify:

```bash
rg -n 'ADZUNA_APP_(ID|KEY)' --glob '!target/**'
mvn -B --no-transfer-progress clean verify
```

The only application configuration forms allowed in source are empty
environment fallbacks:

```yaml
app-id: ${ADZUNA_APP_ID:}
app-key: ${ADZUNA_APP_KEY:}
```

## Remaining external closure evidence

ADZUNA-01 cannot be closed from repository work alone. Closure requires the
restricted revocation/rotation evidence from the provider account
administrator and the SEARCH-02 dependency decision. Repository comments must
record only that the evidence was reviewed, by whom and when—never the values.
