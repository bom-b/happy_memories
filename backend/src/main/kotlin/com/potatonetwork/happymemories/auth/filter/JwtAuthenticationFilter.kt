package com.potatonetwork.happymemories.auth.filter

import com.potatonetwork.happymemories.auth.JwtProvider
import com.potatonetwork.happymemories.auth.service.TokenRefreshService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.LocalDateTime

/**
 * 요청의 HttpOnly 쿠키에서 JWT를 읽어 인증을 처리하는 필터.
 * 액세스 토큰이 만료된 경우 리프레시 토큰을 검증하여 새 액세스 토큰을 투명하게 발급합니다.
 */
@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val tokenRefreshService: TokenRefreshService,
) : OncePerRequestFilter() {

    @Value("\${jwt.access-token-expiry}")
    private var accessTokenExpiry: Long = 0

    @Value("\${jwt.refresh-token-expiry-days}")
    private var refreshTokenExpiryDays: Long = 60

    @Value("\${jwt.cookie-secure:false}")
    private var cookieSecure: Boolean = false

    /**
     * 쿠키에서 액세스 토큰을 확인하고, 만료된 경우 리프레시 토큰으로 자동 갱신합니다.
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val cookies = request.cookies ?: emptyArray()
        val accessToken = cookies.find { it.name == "access_token" }?.value
        val refreshTokenValue = cookies.find { it.name == "refresh_token" }?.value

        if (accessToken != null && jwtProvider.isValid(accessToken)) {
            setAuthentication(jwtProvider.getUserId(accessToken), request)
        } else if (refreshTokenValue != null) {
            attemptSilentRefresh(refreshTokenValue, request, response)
        }

        filterChain.doFilter(request, response)
    }

    /**
     * 리프레시 토큰으로 새 액세스 토큰을 발급하고 만료 일시를 연장합니다.
     * @param refreshTokenValue 쿠키에서 읽은 리프레시 토큰 값
     */
    private fun attemptSilentRefresh(
        refreshTokenValue: String,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val newExpiry = LocalDateTime.now().plusDays(refreshTokenExpiryDays)
        val userId = tokenRefreshService.validateAndRenew(refreshTokenValue, newExpiry) ?: return

        val newAccessToken = jwtProvider.createToken(userId)
        val cookie = ResponseCookie.from("access_token", newAccessToken)
            .httpOnly(true)
            .secure(cookieSecure)
            .path("/")
            .maxAge(accessTokenExpiry / 1000)
            .sameSite("Strict")
            .build()
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())

        setAuthentication(userId, request)
    }

    private fun setAuthentication(userId: String, request: HttpServletRequest) {
        val auth = UsernamePasswordAuthenticationToken(userId, null, emptyList())
        auth.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = auth
    }
}
