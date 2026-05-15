package com.potatonetwork.happymemories.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils

class JwtProviderTest {
    private val jwtProvider = JwtProvider()

    @BeforeEach
    fun setUp() {
        // HMAC-SHA256은 최소 32바이트(256비트) 키가 필요합니다.
        ReflectionTestUtils.setField(jwtProvider, "secret", "test-secret-key-that-is-at-least-32-bytes!!")
        ReflectionTestUtils.setField(jwtProvider, "expiry", 1_800_000L) // 30분
    }

    @Test
    @DisplayName("토큰을 생성하면 동일한 userId를 추출할 수 있다")
    fun createToken_returnsExtractableUserId() {
        val token = jwtProvider.createToken("42")

        assertThat(jwtProvider.getUserId(token)).isEqualTo("42")
    }

    @Test
    @DisplayName("유효한 토큰은 isValid가 true를 반환한다")
    fun isValid_withValidToken_returnsTrue() {
        val token = jwtProvider.createToken("42")

        assertThat(jwtProvider.isValid(token)).isTrue()
    }

    @Test
    @DisplayName("위변조된 토큰은 isValid가 false를 반환한다")
    fun isValid_withTamperedToken_returnsFalse() {
        val token = jwtProvider.createToken("42")

        // 서명 부분을 임의로 변조합니다.
        assertThat(jwtProvider.isValid("$token.tampered")).isFalse()
    }

    @Test
    @DisplayName("만료된 토큰은 isValid가 false를 반환한다")
    fun isValid_withExpiredToken_returnsFalse() {
        ReflectionTestUtils.setField(jwtProvider, "expiry", -1_000L)
        val token = jwtProvider.createToken("42")

        assertThat(jwtProvider.isValid(token)).isFalse()
    }
}
