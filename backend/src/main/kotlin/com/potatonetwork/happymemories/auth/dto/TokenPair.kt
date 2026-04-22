package com.potatonetwork.happymemories.auth.dto

/**
 * 로그인 성공 시 발급되는 액세스 토큰과 리프레시 토큰 쌍.
 */
data class TokenPair(val accessToken: String, val refreshToken: String)
