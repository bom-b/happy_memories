package com.potatonetwork.happymemories.auth.service

import com.potatonetwork.happymemories.user.repository.RefreshTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 필터 레이어에서 트랜잭션이 필요한 리프레시 토큰 DB 작업을 위임받는 서비스.
 */
@Service
class TokenRefreshService(private val refreshTokenRepository: RefreshTokenRepository) {

    /**
     * 리프레시 토큰 값으로 사용자 ID를 조회하고, 유효한 경우 만료 일시를 갱신합니다.
     * @param refreshTokenValue 쿠키에서 읽은 리프레시 토큰 값
     * @param newExpiry 갱신할 새 만료 일시
     * @return 토큰 소유자의 사용자 ID 문자열, 토큰이 없거나 만료됐으면 null
     */
    @Transactional
    fun validateAndRenew(refreshTokenValue: String, newExpiry: LocalDateTime): String? {
        val stored = refreshTokenRepository.findByTokenWithUser(refreshTokenValue).orElse(null) ?: return null
        if (stored.expiresAt.isBefore(LocalDateTime.now())) return null
        refreshTokenRepository.updateExpiresAt(refreshTokenValue, newExpiry)
        return stored.user.id.toString()
    }
}
