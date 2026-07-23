# Adzuna Gateway

Adzuna Gateway isolates Adzuna-specific authentication, request mapping, and
response mapping behind the Job Seeker Copilot provider contract. In fixture
mode it obtains synthetic responses from System Data.

Status: **migration candidate; not beta-ready**. The System Data client is now
generated from a pinned producer contract, while validation, resilience,
licence/attribution, and test gaps remain recorded in
[`docs/BETA_READINESS_AUDIT.md`](docs/BETA_READINESS_AUDIT.md).

## Local verification

```bash
./scripts/test-contract-policy.sh
./scripts/verify-contracts.sh
mvn -B clean verify
docker build -t local/adzuna-gateway .
```

Required live configuration is `ADZUNA_APP_ID` and `ADZUNA_APP_KEY`; neither has
a repository default. `EXTERNAL_PROVIDER_MODE=FIXTURE` is for deterministic
non-production tests, and production must fail closed if fixture mode is
requested.

`develop` is the integration/default branch for beta hardening. See
`CONTRIBUTING.md`, `SECURITY.md`, and the proprietary source-available
`LICENSE`.
