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
import org.mockito.Mockito.anyString
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional

// @InjectMocks: AuthService의 생성자에 @Mock 객체들을 자동으로 주입합니다.
// @Mock: 실제 DB 연결 없이 동작을 직접 지정할 수 있는 가짜 객체입니다.
@ExtendWith(MockitoExtension::class)
class AuthServiceTest {

    @Mock private lateinit var userRepository: UserRepository
    @Mock private lateinit var refreshTokenRepository: RefreshTokenRepository
    @Mock private lateinit var passwordEncoder: PasswordEncoder
    @Mock private lateinit var jwtProvider: JwtProvider

    @InjectMocks private lateinit var authService: AuthService

    /** 테스트용 User 객체 */
    private fun makeUser(id: Long = 1L) = User(
        email = "test@example.com",
        password = "encodedPw",
        name = "테스터",
    ).also { it.id = id }

    @Test
    fun `존재하지 않는 이메일로 로그인하면 BadCredentialsException이 발생한다`() {
        `when`(userRepository.findByEmail(anyString())).thenReturn(Optional.empty())

        assertThatThrownBy { authService.login(LoginRequest("none@test.com", "pw")) }
            .isInstanceOf(BadCredentialsException::class.java)
    }

    @Test
    fun `비밀번호가 틀리면 BadCredentialsException이 발생한다`() {
        `when`(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(makeUser()))
        `when`(passwordEncoder.matches("wrongPw", "encodedPw")).thenReturn(false)

        assertThatThrownBy { authService.login(LoginRequest("test@example.com", "wrongPw")) }
            .isInstanceOf(BadCredentialsException::class.java)
    }

    @Test
    fun `올바른 자격증명으로 로그인하면 TokenPair를 반환하고 리프레시 토큰을 저장한다`() {
        `when`(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(makeUser()))
        `when`(passwordEncoder.matches("pw", "encodedPw")).thenReturn(true)
        `when`(jwtProvider.createToken("1")).thenReturn("mocked-access-token")

        val result = authService.login(LoginRequest("test@example.com", "pw"))

        assertThat(result.accessToken).isEqualTo("mocked-access-token")
        // 리프레시 토큰이 DB에 저장되는지 확인합니다.
        verify(refreshTokenRepository).save(org.mockito.Mockito.any(RefreshToken::class.java))
    }

    @Test
    fun `logout을 호출하면 해당 리프레시 토큰이 삭제된다`() {
        authService.logout("some-refresh-token")

        verify(refreshTokenRepository).deleteByToken("some-refresh-token")
    }
}
