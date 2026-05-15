package com.potatonetwork.happymemories.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils

class JwtProviderTest {

    // Spring 컨텍스트 없이 순수 객체로 테스트합니다.
    // @Value 필드는 ReflectionTestUtils로 직접 주입합니다.
    private val jwtProvider = JwtProvider()

    @BeforeEach
    fun setUp() {
        // HMAC-SHA256은 최소 32바이트(256비트) 키가 필요합니다.
        ReflectionTestUtils.setField(jwtProvider, "secret", "test-secret-key-that-is-at-least-32-bytes!!")
        ReflectionTestUtils.setField(jwtProvider, "expiry", 1_800_000L) // 30분
    }

    @Test
    fun `토큰을 생성하면 동일한 userId를 추출할 수 있다`() {
        val token = jwtProvider.createToken("42")

        assertThat(jwtProvider.getUserId(token)).isEqualTo("42")
    }

    @Test
    fun `유효한 토큰은 isValid가 true를 반환한다`() {
        val token = jwtProvider.createToken("42")

        assertThat(jwtProvider.isValid(token)).isTrue()
    }

    @Test
    fun `위변조된 토큰은 isValid가 false를 반환한다`() {
        val token = jwtProvider.createToken("42")

        // 서명 부분을 임의로 변조합니다.
        assertThat(jwtProvider.isValid("$token.tampered")).isFalse()
    }

    @Test
    fun `만료된 토큰은 isValid가 false를 반환한다`() {
        ReflectionTestUtils.setField(jwtProvider, "expiry", -1_000L)
        val token = jwtProvider.createToken("42")

        assertThat(jwtProvider.isValid(token)).isFalse()
    }
}
