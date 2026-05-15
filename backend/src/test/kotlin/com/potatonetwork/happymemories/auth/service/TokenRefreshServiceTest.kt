package com.potatonetwork.happymemories.auth.service

import com.potatonetwork.happymemories.user.entity.RefreshToken
import com.potatonetwork.happymemories.user.entity.User
import com.potatonetwork.happymemories.user.repository.RefreshTokenRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class TokenRefreshServiceTest {

    @Mock
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @InjectMocks
    private lateinit var tokenRefreshService: TokenRefreshService

    private val newExpiry = LocalDateTime.now().plusDays(60)

    /** 테스트용 User 객체를 생성 */
    private fun makeUser(id: Long = 1L) = User(
        email = "test@example.com",
        password = "encodedPw",
        name = "테스터",
    ).also { it.id = id }

    @Test
    @DisplayName("존재하지 않는 토큰이면 null을 반환한다")
    fun validateAndRenew_withUnknownToken_returnsNull() {
        `when`(refreshTokenRepository.findByTokenWithUser("unknown-token")).thenReturn(Optional.empty())

        val result = tokenRefreshService.validateAndRenew("unknown-token", newExpiry)

        assertThat(result).isNull()
    }

    @Test
    @DisplayName("만료된 토큰이면 null을 반환하고 갱신하지 않는다")
    fun validateAndRenew_withExpiredToken_returnsNullAndSkipsUpdate() {
        val expiredToken = RefreshToken(
            user = makeUser(),
            token = "expired-token",
            expiresAt = LocalDateTime.now().minusDays(1), // 이미 만료됨
        )
        `when`(refreshTokenRepository.findByTokenWithUser("expired-token")).thenReturn(Optional.of(expiredToken))

        val result = tokenRefreshService.validateAndRenew("expired-token", newExpiry)

        assertThat(result).isNull()

        verify(refreshTokenRepository).findByTokenWithUser("expired-token")
        verifyNoMoreInteractions(refreshTokenRepository)
    }

    @Test
    @DisplayName("유효한 토큰이면 userId를 반환하고 만료 일시를 갱신한다")
    fun validateAndRenew_withValidToken_returnsUserIdAndUpdatesExpiry() {
        val validToken = RefreshToken(
            user = makeUser(id = 7L),
            token = "valid-token",
            expiresAt = LocalDateTime.now().plusDays(30), // 아직 유효함
        )
        `when`(refreshTokenRepository.findByTokenWithUser("valid-token")).thenReturn(Optional.of(validToken))

        val result = tokenRefreshService.validateAndRenew("valid-token", newExpiry)

        assertThat(result).isEqualTo("7")
        verify(refreshTokenRepository).updateExpiresAt("valid-token", newExpiry)
    }
}
