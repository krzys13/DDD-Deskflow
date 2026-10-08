package com.deskflow.identity.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-key-at-least-32-characters-long-for-hs256-tests";

    @Test
    void generated_token_contains_email_and_role() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 60_000);

        String token = provider.issueAccessToken("user@test.pl", "IT_ADMIN");

        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getEmailFromToken(token)).isEqualTo("user@test.pl");
        assertThat(provider.getRoleFromToken(token)).isEqualTo("IT_ADMIN");
    }

    @Test
    void invalid_token_is_rejected() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 60_000);

        assertThat(provider.validateToken("not-a-jwt")).isFalse();
    }

    @Test
    void expired_token_is_rejected() throws InterruptedException {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 1);
        String token = provider.issueAccessToken("user@test.pl", "EMPLOYEE");

        Thread.sleep(5);

        assertThat(provider.validateToken(token)).isFalse();
    }
}
