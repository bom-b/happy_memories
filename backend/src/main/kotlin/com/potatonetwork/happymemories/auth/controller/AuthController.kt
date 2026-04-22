package com.potatonetwork.happymemories.auth.controller

import com.potatonetwork.happymemories.auth.dto.LoginRequest
import com.potatonetwork.happymemories.auth.service.AuthService
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 인증 관련 엔드포인트를 제공하는 컨트롤러.
 * 로그인 성공 시 액세스 토큰과 리프레시 토큰을 HttpOnly 쿠키에 설정합니다.
 */
@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {

    @Value("\${jwt.access-token-expiry}")
    private var accessTokenExpiry: Long = 0

    @Value("\${jwt.refresh-token-expiry-days}")
    private var refreshTokenExpiryDays: Long = 60

    @Value("\${jwt.cookie-secure:false}")
    private var cookieSecure: Boolean = false

    /**
     * 이메일·비밀번호로 로그인하고 액세스 토큰과 리프레시 토큰을 HttpOnly 쿠키에 설정합니다.
     * @param request 이메일과 비밀번호 DTO
     * @return 200 OK
     */
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        response: HttpServletResponse,
    ): ResponseEntity<Void> {
        val (accessToken, refreshToken) = authService.login(request)
        response.addHeader(HttpHeaders.SET_COOKIE, buildAccessTokenCookie(accessToken).toString())
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshTokenCookie(refreshToken).toString())
        return ResponseEntity.ok().build()
    }

    /**
     * 현재 디바이스의 리프레시 토큰을 무효화하고 쿠키를 만료 처리합니다.
     * @return 200 OK
     */
    @PostMapping("/logout")
    fun logout(
        @CookieValue(name = "refresh_token", required = false) refreshTokenValue: String?,
        response: HttpServletResponse,
    ): ResponseEntity<Void> {
        refreshTokenValue?.let { authService.logout(it) }
        response.addHeader(HttpHeaders.SET_COOKIE, buildExpiredCookie("access_token").toString())
        response.addHeader(HttpHeaders.SET_COOKIE, buildExpiredCookie("refresh_token").toString())
        return ResponseEntity.ok().build()
    }

    private fun buildAccessTokenCookie(token: String): ResponseCookie =
        ResponseCookie.from("access_token", token)
            .httpOnly(true)
            .secure(cookieSecure)
            .path("/")
            .maxAge(accessTokenExpiry / 1000)
            .sameSite("Strict")
            .build()

    private fun buildRefreshTokenCookie(token: String): ResponseCookie =
        ResponseCookie.from("refresh_token", token)
            .httpOnly(true)
            .secure(cookieSecure)
            .path("/")
            .maxAge(refreshTokenExpiryDays * 86400)
            .sameSite("Strict")
            .build()

    private fun buildExpiredCookie(name: String): ResponseCookie =
        ResponseCookie.from(name, "")
            .httpOnly(true)
            .path("/")
            .maxAge(0)
            .build()
}
