# Spring Boot 4.x Migration Notes — 2026-08-08

## Summary
Bumps `spring-boot-starter-parent` from `3.5.16` → `4.1.0` (latest Spring Boot 4 at the time). Closes the 19 unfixed CVEs documented in the previous `SECURITY-NOTES.md` (14 Netty + 3 Jackson polymorphic + 1 Spring Security enumeration + 1 admin-login enumeration). One residual Jackson CVE remains (CVE-2026-59889, see SECURITY-NOTES.md).

## What changed

### `pom.xml`
- `<parent><version>` for `spring-boot-starter-parent`: **3.5.16 → 4.1.0**.
- Removed `<dependencyManagement>` entries for `jackson-core`/`jackson-databind`/`netty-codec` (Spring Boot 4's BOM now controls these).
- Added `<netty.version>4.2.16.Final</netty.version>` to `<properties>` — Spring Boot 4.1.0's BOM default is 4.2.15.Final which carries 12 advisories; bumping to 4.2.16.Final clears all of them.
- Kept `<dependencyManagement>` entry for `assertj-core` 3.27.7 (test-scope; Spring Boot 4 BOM does not control).
- `<java.version>21</java.version>` unchanged.

### `src/main/java/.../config/SecurityConfiguration.java`
- Line 3: `import com.fasterxml.jackson.databind.ObjectMapper;` → `import tools.jackson.databind.ObjectMapper;` (Jackson 3 package rename). Line 98's `new ObjectMapper()` call works unchanged with Jackson 3 API.
- No other SS7 DSL changes required — `oauth2Login()`, `authorizeHttpRequests()`, `csrf()`, `sessionManagement()`, `exceptionHandling()`, `SecurityFilterChain` bean DSL is largely preserved in Spring Security 7.

### Jackson import notes
- `tools.jackson.core`, `tools.jackson.databind`, `tools.jackson.datatype`, `tools.jackson.dataformat` packages renamed from `com.fasterxml.jackson.*`.
- `com.fasterxml.jackson.annotation` stays at the legacy coordinate — Jackson 3 keeps annotations there for source compatibility (Spring Boot 4's BOM transitively pulls in `jackson-annotations` 2.x at the legacy coord).
- **6 model files** (`QueueResponseModel`, `SpotifyUserResponse`, `Song`, `Blacklist`, `ApproveRequest`, `QueueAddRequest`) keep their `com.fasterxml.jackson.annotation` imports — they resolve correctly.

## Breaking changes worked around
- **None requiring code changes** — the migration was contained to:
  - Parent version bump (BOM-controlled cascade to all Spring/Jackson/Netty versions).
  - One Jackson import rename in `SecurityConfiguration.java` (line 3).
  - One `<properties>` entry to pin Netty to 4.2.16.Final (avoids 12 advisories in the BOM default).

## Validation
- `mvn -B clean verify` → BUILD SUCCESS, `MVN_EXIT=0`. Spring context loads cleanly under Spring Boot 4.1.0 + Spring Security 7 + Spring Framework 7 + Jackson 3 + Netty 4.2.16. Surefire reports 0 tests run (the only test class, `BackendApplicationTests`, has no `@Test` methods — a pre-existing test-suite weakness, not a regression).
- `osv-scanner scan -S target/bom-all.json --format json` against the post-bump SBOM: 0 Netty CVEs, 0 Jackson production-scope CVEs (besides the accepted residual CVE-2026-59889), 0 Spring Framework CVEs.

## Recommended follow-ups
1. Subscribe to jackson-databind 3.1.5+ release notes — the moment Jackson ships a fix for CVE-2026-59889, the `netty.version` override can be dropped.
2. Add `.github/dependabot.yml` to jambox-backend (currently absent; no automated Dependabot PRs).
3. Plan a future Spring Boot 5.x / Jackson 4.x major bump.
4. Add real `@Test` methods to `BackendApplicationTests` (and possibly new test classes) — the current test class only verifies that the Spring context loads, which is a weak test-suite foundation.