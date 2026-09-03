// System Design - Backend
// Chapter 22, Automated Testing -> 14 Mocking time, randomness & external APIs
// Java 21 / JUnit 5 / AssertJ / Mockito / Testcontainers

package com.example.testing;

// 1) An injected Clock makes time deterministic. java.time.Clock exists
//    for exactly this, so there is no custom interface to write.
class TokenTest {
    
    @Test
    void expiryIsComputedFromTheInjectedClock() {
        Clock frozen = Clock.fixed(
            Instant.parse("2026-01-01T12:00:00Z"), ZoneOffset.UTC);
        
        Token token = Tokens.issue(frozen, Duration.ofHours(1));
        
        assertThat(token.expiresAt())
            .isEqualTo(frozen.instant().plus(Duration.ofHours(1)));
    }
}

// 2) Stub an external HTTP API with WireMock. It is a real server on
// localhost, so your client's own HTTP stack is exercised too.
@WireMockTest
class RatesClientTest {
    
    @Test
    void parsesTheRateFromTheResponse(WireMockRuntimeInfo wm) {
        stubFor(get("/usd-inr").willReturn(okJson("""
            {"usd_inr": 83.2}
            """))); // the canned response
        
        RatesClient client = new RatesClient(wm.getHttpBaseUrl());
        
        assertThat(client.usdInr()).isEqualTo(83.2);
    }
}
