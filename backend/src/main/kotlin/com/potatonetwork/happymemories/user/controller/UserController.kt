package com.potatonetwork.happymemories.user.controller

import com.potatonetwork.happymemories.user.dto.UserProfileResponse
import com.potatonetwork.happymemories.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 사용자 관련 엔드포인트를 제공하는 컨트롤러.
 */
@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {

    /**
     * 현재 인증된 사용자의 공개 프로필 정보를 반환합니다.
     * @param authentication SecurityContext에서 주입되는 인증 객체
     * @return 이름과 이메일이 담긴 프로필 응답 DTO
     */
    @GetMapping("/me")
    fun getMe(authentication: Authentication): ResponseEntity<UserProfileResponse> {
        val userId = authentication.name.toLong()
        return ResponseEntity.ok(userService.getProfile(userId))
    }
}
