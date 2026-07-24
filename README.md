# Adzuna Gateway

Adzuna Gateway isolates Adzuna-specific authentication, request mapping, and
response mapping behind the Job Seeker Copilot provider contract. In fixture
mode it obtains synthetic responses from System Data.

Status: **migration candidate; not beta-ready**. The System Data client is now
generated from a pinned producer contract. Deterministic provider mapping,
empty-result, error-translation, gateway-contract, and populated/empty System
Data fixture tests run without live provider calls. Validation, resilience,
licence/attribution, and remaining beta gaps are recorded in
[`docs/BETA_READINESS_AUDIT.md`](docs/BETA_READINESS_AUDIT.md).

Its provider-specific ownership and the boundary with canonical Job Service
results are defined in the Infrastructure
[Job Search architecture ADR](https://github.com/jobseekercopilot/infrastructure/blob/develop/docs/adr/0001-job-search-architecture-and-ownership.md).

## Local verification

```bash
./scripts/test-contract-policy.sh
./scripts/verify-contracts.sh
mvn -B clean verify
docker build -t local/adzuna-gateway .
```

`mvn -B clean verify` currently runs seven offline tests. Synthetic provider
HTTP responses cover representative, missing and malformed fields, salary,
location, date, source URL, zero-result and provider-error behaviour. Generated
System Data types are referenced directly by fixture tests, so incompatible
producer contract changes fail compilation.

Required live configuration is `ADZUNA_APP_ID` and `ADZUNA_APP_KEY`; neither has
a repository default. `EXTERNAL_PROVIDER_MODE=FIXTURE` is for deterministic
non-production tests, and production must fail closed if fixture mode is
requested.

`develop` is the integration/default branch for beta hardening. See
`CONTRIBUTING.md`, `SECURITY.md`, and the proprietary source-available
`LICENSE`.
