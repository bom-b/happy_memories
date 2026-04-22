package com.potatonetwork.happymemories.user.service

import com.potatonetwork.happymemories.user.dto.UserProfileResponse
import com.potatonetwork.happymemories.user.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스.
 */
@Service
class UserService(private val userRepository: UserRepository) {

    /**
     * 사용자 ID로 공개 프로필 정보를 조회합니다.
     * @param userId 조회할 사용자 ID
     * @return 이름과 이메일이 담긴 프로필 응답 DTO
     * @throws EntityNotFoundException 사용자가 존재하지 않는 경우
     */
    @Transactional(readOnly = true)
    fun getProfile(userId: Long): UserProfileResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { EntityNotFoundException("사용자를 찾을 수 없습니다.") }
        return UserProfileResponse(name = user.name, email = user.email)
    }
}
