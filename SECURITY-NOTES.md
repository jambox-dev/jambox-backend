# Security Notes — 2026-08-08 (post Spring Boot 4 upgrade)

This PR bumps `spring-boot-starter-parent` from **3.5.16** to **4.1.0** and adds a `<netty.version>4.2.16.Final</netty.version>` property override (the Spring Boot 4.1.0 BOM ships Netty 4.2.15.Final which carries 12 advisories; bumping to 4.2.16.Final clears all of them). Jackson imports migrated `com.fasterxml.jackson.*` → `tools.jackson.*` (Jackson 3). GitHub Actions unchanged (Todo 6 from previous plan still in effect).

## Post-bump `osv-scanner` results (run on CycloneDX SBOM of `target/bom-all.json`)

| Package family | CVEs remaining | Status |
|---|---|---|
| Netty | 0 | closed (overridden to 4.2.16.Final) |
| Jackson | 1 | residual — see below |
| Spring Framework / Security | 0 | closed transitively by 3.5.6 → 4.1.0 |
| Spring Data MongoDB | 0 | closed (3.5.4 → 4.x via 4.1.0 BOM) |
| Tomcat / Spring Framework web | 0 | closed transitively |

## Residual CVE (accepted)

| CVE | GHSA | Package | Version | Summary | Why not fixed |
|---|---|---|---|---|---|
| CVE-2026-59889 | (no GHSA) | `tools.jackson.core:jackson-databind` | 3.1.4 (latest) | `@JsonView` annotation can be bypassed via `@JsonUnwrapped` polymorphism (CWE-863, MODERATE) | Jackson 3.1.4 is the latest available version with no upstream fix published. Upstream-tracking issue: https://github.com/FasterXML/jackson-databind/issues (subscribe to the 3.1.5+ release notes) |

## Recommended follow-up actions

1. **Subscribe to jackson-databind 3.1.5+ release notes** — the moment Jackson ships a fix for CVE-2026-59889, run `mvn -B clean verify` after dropping the `netty.version` override (Spring Boot 4 BOM will pick up the latest Jackson automatically).
2. **Re-run `osv-scanner` weekly** to catch any new in-major fixes that land on Netty 4.1.x (currently at 4.2.16.Final).
3. **Open a follow-up plan** for the next major bump (Spring Boot 5.x / Jackson 4.x) when those lines GA.
4. **Enable Dependabot on jambox-backend** — the `.github/dependabot.yml` is currently absent, so this repo has no automated Dependabot PRs.