package com.potatonetwork.happymemories.auth.service

import com.potatonetwork.happymemories.auth.JwtProvider
import com.potatonetwork.happymemories.auth.dto.LoginRequest
import com.potatonetwork.happymemories.auth.dto.TokenPair
import com.potatonetwork.happymemories.user.entity.RefreshToken
import com.potatonetwork.happymemories.user.repository.RefreshTokenRepository
import com.potatonetwork.happymemories.user.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

/**
 * 인증 관련 비즈니스 로직을 처리하는 서비스.
 */
@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider,
) {

    @Value("\${jwt.refresh-token-expiry-days}")
    private var refreshTokenExpiryDays: Long = 60

    /**
     * 이메일·비밀번호를 검증하고 액세스 토큰과 리프레시 토큰을 발급합니다.
     * 리프레시 토큰은 DB에 저장되며, 디바이스별로 독립적으로 관리됩니다.
     * @param request 이메일과 비밀번호 DTO
     * @return 액세스 토큰과 리프레시 토큰 쌍
     * @throws BadCredentialsException 이메일이 존재하지 않거나 비밀번호가 틀린 경우
     */
    @Transactional
    fun login(request: LoginRequest): TokenPair {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.") }

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.")
        }

        val accessToken = jwtProvider.createToken(user.id.toString())
        val refreshTokenValue = UUID.randomUUID().toString()

        refreshTokenRepository.save(
            RefreshToken(
                user = user,
                token = refreshTokenValue,
                expiresAt = LocalDateTime.now().plusDays(refreshTokenExpiryDays),
            )
        )

        return TokenPair(accessToken, refreshTokenValue)
    }

    /**
     * 리프레시 토큰 값으로 해당 디바이스의 세션을 로그아웃 처리합니다.
     * @param refreshTokenValue 무효화할 리프레시 토큰 값
     */
    @Transactional
    fun logout(refreshTokenValue: String) {
        refreshTokenRepository.deleteByToken(refreshTokenValue)
    }
}
