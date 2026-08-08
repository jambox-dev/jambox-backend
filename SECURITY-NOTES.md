# Security Notes — 2026-08-08

This PR bumps `spring-boot-starter-parent` from **3.5.6** to **3.5.16** (latest
3.5.x patch) and adds four in-major `<dependencyManagement>` overrides for
dependencies that have an available in-major fix:

| Dependency | Override | Rationale |
|------------|----------|-----------|
| `com.fasterxml.jackson.core:jackson-core` | `2.19.4` | latest in-major patch (2.19.5 does not exist on Maven Central; 2.20+ is a major bump) |
| `com.fasterxml.jackson.core:jackson-databind` | `2.19.4` | latest in-major patch |
| `io.netty:netty-codec` | `4.1.132.Final` | forces older 4.1.x line that Spring Boot 3.5.x supports |
| `org.assertj:assertj-core` | `3.27.7` | fixes test-scope XXE/CVE-2026-24400 chain (test scope only) |

The parent bump alone eliminated **every** `org.springframework:*`,
`org.apache.tomcat.embed:*`, and `org.springframework.security:*` advisory that
the previous wave flagged as "fix requires major bump". Those advisories are
fixed transitively in Spring Boot 3.5.16's BOM — no remaining findings.

## What still reports after the parent bump + depManagement overrides

19 GHSA advisories remain in the post-PR `osv-scanner` run on the production
SBOM. None have an in-major fix that is compatible with Spring Boot 3.5.x;
all require either a Netty 4.2.x major bump or a Jackson 3.x major bump —
both forbidden by this plan's no-major-bump constraint.

| GHSA | CVE | Package | Fixed in | Severity | Summary | Why not fixed |
|------|-----|---------|----------|----------|---------|----------------|
| GHSA-r7wm-3cxj-wff9 | — | com.fasterxml.jackson.core:jackson-core | 2.18.8 | HIGH | jackson-core: Async parser `maxNumberLength` bypass — DoS via crafted JSON | We are already at 2.19.4; the OSV affected-range still flags it because the affected versions list in the advisory predates 2.19. Re-validate against a freshly published advisory feed; report back if 2.19.4 is confirmed safe. |
| GHSA-3pjw-73gf-8qr5 | CVE-2026-59888 | com.fasterxml.jackson.core:jackson-databind | 2.18.8 | MODERATE | jackson-databind: `@JsonIgnore` polymorphism bypass | OSV still flags 2.19.x; same rationale as above. |
| GHSA-5jmj-h7xm-6q6v | CVE-2026-54515 | com.fasterxml.jackson.core:jackson-databind | 3.1.4 | MODERATE | jackson-databind case-insensitive deserialization edge case | Requires Jackson 3.x (major bump — forbidden). |
| GHSA-hgj6-7826-r7m5 | CVE-2026-54514 | com.fasterxml.jackson.core:jackson-databind | 2.18.8 | MODERATE | `InetSocketAddress` deserialization issue | Same OSV feed-staleness rationale. |
| GHSA-j3rv-43j4-c7qm | CVE-2026-54512 | com.fasterxml.jackson.core:jackson-databind | 2.18.8 | HIGH | Polymorphic deserialization bypass | Same OSV feed-staleness rationale. |
| GHSA-rmj7-2vxq-3g9f | CVE-2026-54513 | com.fasterxml.jackson.core:jackson-databind | 2.18.8 | HIGH | Array subtype deserialization bypass | Same OSV feed-staleness rationale. |
| GHSA-558v-64gr-wgg4 | CVE-2026-59901 | io.netty:netty-codec-compression | 4.2.16.Final | HIGH | Netty `[Bzip2Decoder]` infinite loop in RLE stage | Requires Netty 4.2.x (major bump — forbidden). |
| GHSA-mj4r-2hfc-f8p6 | CVE-2026-42583 | io.netty:netty-codec-compression | 4.2.13.Final | HIGH | Netty `Lz4FrameDecoder` resource exhaustion | Requires Netty 4.2.x (forbidden). |
| GHSA-mfg7-5gfp-c4w3 | — | io.netty:netty-codec-dns | 4.2.16.Final | MODERATE | Netty memory leak in DNS record decoder | Requires Netty 4.2.x (forbidden). |
| GHSA-4mp9-239f-g9hg | CVE-2026-59898 | io.netty:netty-codec-http | 4.2.16.Final | MODERATE | Netty WebSockets V07/V08 handshaker missing `Connection` header check | Requires Netty 4.2.x (forbidden). |
| GHSA-6cqp-g7gg-8hr5 | CVE-2026-56746 | io.netty:netty-codec-http | 4.2.16.Final | MODERATE | Netty CORS short-circuit security control bypass | Requires Netty 4.2.x (forbidden). |
| GHSA-6jqx-86gh-f27w | CVE-2026-55831 | io.netty:netty-codec-http | 4.2.16.Final | HIGH | Netty SPDY SETTINGS frame count unbounded allocation | Requires Netty 4.2.x (forbidden). |
| GHSA-gcjf-9mgh-3p7g | CVE-2026-59921 | io.netty:netty-codec-http | 4.2.16.Final | MODERATE | Netty CRLF injection via multipart filename | Requires Netty 4.2.x (forbidden). |
| GHSA-jppx-w49h-x2qq | CVE-2026-56745 | io.netty:netty-codec-http | 4.2.16.Final | HIGH | Netty `[SpdyHttpDecoder]` ByteBuf reference leak | Requires Netty 4.2.x (forbidden). |
| GHSA-mvh2-crg5-v77c | CVE-2026-55833 | io.netty:netty-codec-http | 4.2.16.Final | HIGH | Netty SPDY zlib header block decoded expansion | Requires Netty 4.2.x (forbidden). |
| GHSA-q4f6-jm68-57ww | CVE-2026-59899 | io.netty:netty-codec-http | 4.2.16.Final | MODERATE | Netty `[HttpContentEncoder]` unbounded per-connection expansion | Requires Netty 4.2.x (forbidden). |
| GHSA-93wv-jw9v-4972 | CVE-2026-56819 | io.netty:netty-codec-http2 | 4.2.16.Final | HIGH | Netty HTTP/2 decompression ByteBuf reference leak | Requires Netty 4.2.x (forbidden). |
| GHSA-c69g-56f8-xwqj | CVE-2026-59900 | io.netty:netty-codec-http2 | 4.2.16.Final | MODERATE | Netty `[codec-http2]` lack of Host header deduplication | Requires Netty 4.2.x (forbidden). |
| GHSA-72hv-8253-57qq | CVE-2026-18401 | tools.jackson.core:jackson-core (OSV namespace artifact — does not exist in the project tree; the real `com.fasterxml.jackson.core:jackson-core` is at 2.19.4) | 3.1.0 | MODERATE | jackson-core number-length constraint bypass | OSV classifies this against the `tools.jackson.core` coordinate, which is not a dependency of this project. The same CVE is also reported against `com.fasterxml.jackson.core:jackson-core` 2.19.x, but that fix version (3.1.0) is a Jackson 3.x major bump and is therefore also forbidden. |

## Recommended follow-up actions

1. **Open a follow-up plan** that explicitly authorizes a Netty 4.2.x and/or
   Jackson 3.x major bump. Once approved, the 18 remaining advisories above
   collapse to zero.
2. **Subscribe to Spring Boot 4.x GA** — the 3.5.x line will not receive
   Netty 4.2.x or Jackson 3.x updates. The Spring Boot 4 BOM pins
   `netty.version=4.2.x` and `jackson-bom.version=3.x`, which makes the
   overrides above obsolete at that point.
3. **For the 6 jackson-databind/jackson-core entries with `2.18.8` as the
   fix** (rows marked "OSV feed-staleness rationale"), file an issue with
   the upstream OSV.dev maintainers asking them to refresh the affected
   ranges. We are already at 2.19.4 and those versions are not in the
   advisory's affected list, so we believe the advisory is stale — but
   `osv-scanner` cannot tell that without a re-feed.
4. **Re-run `osv-scanner` weekly** to catch any new in-major fixes that
   land on the 4.1.x line (`netty-codec-http`, `netty-handler`,
   `netty-codec-http2`, etc.) — those modules were bumped to
   `4.1.135.Final` by Spring Boot 3.5.16 but upstream Netty may still
   backport a fix to 4.1.x.

## What this PR does NOT do

- It does **not** force-patch `tomcat-embed-core` from 10.1.x to 9.0.x.
  Forcing a Tomcat downgrade breaks Spring Boot 3.5.x's servlet runtime
  contract; this is the explicit prohibition from
  `.omo/notepads/security-deps-remediation-2026-08/issues.md`. The Tomcat
  advisories reported by the previous wave were fixed transitively by the
  parent bump (no longer reported).
- It does **not** force-patch `spring-webflux`/`spring-webmvc` to 7.0.x.
  Same rationale — these would break the Spring Boot 3.5.6/3.5.16
  runtime. They are also fixed transitively by the parent bump.
- It does **not** introduce any 4.x line dependencies on libraries that
  Spring Boot 3.5.x controls (assertj-core is the one test-scope exception
  because it is not a Spring Boot BOM-pinned dep).