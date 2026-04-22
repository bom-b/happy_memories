package com.potatonetwork.happymemories.user.repository

import com.potatonetwork.happymemories.user.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime
import java.util.Optional

/**
 * 리프레시 토큰 데이터 접근 인터페이스.
 */
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {

    /**
     * 토큰 값으로 리프레시 토큰을 조회합니다. 연관된 User를 함께 페치합니다.
     * @param token 조회할 토큰 값
     */
    @Query("SELECT r FROM RefreshToken r JOIN FETCH r.user WHERE r.token = :token")
    fun findByTokenWithUser(token: String): Optional<RefreshToken>

    /**
     * 토큰 값으로 만료 일시를 갱신합니다.
     * @param token 갱신할 토큰 값
     * @param expiresAt 새 만료 일시
     */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.expiresAt = :expiresAt WHERE r.token = :token")
    fun updateExpiresAt(token: String, expiresAt: LocalDateTime)

    /**
     * 토큰 값으로 리프레시 토큰을 삭제합니다 (디바이스 로그아웃).
     * @param token 삭제할 토큰 값
     */
    fun deleteByToken(token: String)
}
