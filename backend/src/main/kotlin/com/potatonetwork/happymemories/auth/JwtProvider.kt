package com.potatonetwork.happymemories.auth

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

/**
 * JWT 토큰 생성·검증 컴포넌트.
 * application.yaml의 jwt.secret 값으로 HMAC-SHA256 서명을 수행합니다.
 */
@Component
class JwtProvider {

    @Value("\${jwt.secret}")
    private lateinit var secret: String

    @Value("\${jwt.access-token-expiry}")
    private var expiry: Long = 0

    private val key by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8))
    }

    /**
     * 사용자 ID를 subject로 담은 JWT를 생성합니다.
     * @param userId 토큰에 담을 사용자 ID (문자열)
     * @return 서명된 JWT 문자열
     */
    fun createToken(userId: String): String =
        Jwts.builder()
            .subject(userId)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expiry))
            .signWith(key)
            .compact()

    /**
     * 토큰에서 사용자 ID를 추출합니다.
     * @param token 검증된 JWT 문자열
     * @return subject에 담긴 사용자 ID
     */
    fun getUserId(token: String): String =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject

    /**
     * 토큰의 서명과 만료 여부를 검증합니다.
     * @param token 검증할 JWT 문자열
     * @return 유효하면 true, 그렇지 않으면 false
     */
    fun isValid(token: String): Boolean =
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
            true
        } catch (e: JwtException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
}
