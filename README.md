# Adzuna Gateway

Adzuna Gateway isolates Adzuna-specific authentication, request mapping, and
response mapping behind the Job Seeker Copilot provider contract. In fixture
mode it obtains synthetic responses from System Data.

Status: **implemented and composed for controlled private-beta use**. Fixture
mode is the deterministic default and the live path has been exercised in a
bounded manual validation; this is not a claim of provider reliability or
production deployment approval. The System Data client is generated from a
pinned producer contract. Deterministic provider mapping,
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

`mvn -B clean verify` runs offline tests. Synthetic provider HTTP responses
cover representative, missing and malformed fields, salary, location, date,
source URL, zero-result and provider-error behaviour. Generated System Data
types are referenced directly by fixture tests, so incompatible producer
contract changes fail compilation.

The safe default is `EXTERNAL_PROVIDER_MODE=FIXTURE`, which requires no live
credential and is restricted to non-production use. Enabled `LIVE` mode
requires both `ADZUNA_APP_ID` and `ADZUNA_APP_KEY` before startup succeeds;
neither has a non-empty repository default. `ADZUNA_ENABLED=false` is the
provider kill switch. `ADZUNA_RESULTS_PER_PAGE` defaults to 50 and is always
clamped to the provider range of one to 50, including explicit gateway
requests. `ADZUNA_PAGES_PER_SEARCH` defaults to one and is always clamped to
the range one to two so a logical search cannot fan out without a hard bound.
Rotation, restricted evidence, renewal, history
verification and incident procedures are defined in
[`docs/CREDENTIAL_OPERATIONS.md`](docs/CREDENTIAL_OPERATIONS.md).

`develop` is the integration/default branch for beta hardening. See
`CONTRIBUTING.md`, `SECURITY.md`, and the proprietary source-available
`LICENSE`.
