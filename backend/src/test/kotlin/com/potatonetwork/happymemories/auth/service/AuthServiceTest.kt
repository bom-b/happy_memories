package com.potatonetwork.happymemories.auth.service

import com.potatonetwork.happymemories.auth.JwtProvider
import com.potatonetwork.happymemories.auth.dto.LoginRequest
import com.potatonetwork.happymemories.user.entity.RefreshToken
import com.potatonetwork.happymemories.user.entity.User
import com.potatonetwork.happymemories.user.repository.RefreshTokenRepository
import com.potatonetwork.happymemories.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository
    @Mock
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    @Mock
    private lateinit var passwordEncoder: PasswordEncoder
    @Mock
    private lateinit var jwtProvider: JwtProvider

    @InjectMocks
    private lateinit var authService: AuthService

    /** 테스트용 User 객체 */
    private fun makeUser(id: Long = 1L) = User(
        email = "test@example.com",
        password = "encodedPw",
        name = "테스터",
    ).also { it.id = id }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인하면 BadCredentialsException이 발생한다")
    fun login_withUnknownEmail_throwsBadCredentialsException() {
        `when`(userRepository.findByEmail(anyString())).thenReturn(Optional.empty())

        assertThatThrownBy { authService.login(LoginRequest("none@test.com", "pw")) }
            .isInstanceOf(BadCredentialsException::class.java)
    }

    @Test
    @DisplayName("비밀번호가 틀리면 BadCredentialsException이 발생한다")
    fun login_withWrongPassword_throwsBadCredentialsException() {
        `when`(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(makeUser()))
        `when`(passwordEncoder.matches("wrongPw", "encodedPw")).thenReturn(false)

        assertThatThrownBy { authService.login(LoginRequest("test@example.com", "wrongPw")) }
            .isInstanceOf(BadCredentialsException::class.java)
    }

    @Test
    @DisplayName("올바른 자격증명으로 로그인하면 TokenPair를 반환하고 리프레시 토큰을 저장한다")
    fun login_withValidCredentials_returnsTokenPairAndSavesRefreshToken() {
        `when`(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(makeUser()))
        `when`(passwordEncoder.matches("pw", "encodedPw")).thenReturn(true)
        `when`(jwtProvider.createToken("1")).thenReturn("mocked-access-token")

        val result = authService.login(LoginRequest("test@example.com", "pw"))

        assertThat(result.accessToken).isEqualTo("mocked-access-token")
        // 리프레시 토큰이 DB에 저장되는지 확인합니다.
        verify(refreshTokenRepository).save(any(RefreshToken::class.java))
    }

    @Test
    @DisplayName("logout을 호출하면 해당 리프레시 토큰이 삭제된다")
    fun logout_deletesRefreshToken() {
        authService.logout("some-refresh-token")

        verify(refreshTokenRepository).deleteByToken("some-refresh-token")
    }
}
