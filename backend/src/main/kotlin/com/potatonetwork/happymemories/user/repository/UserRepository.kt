package com.potatonetwork.happymemories.user.repository

import com.potatonetwork.happymemories.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

/**
 * 사용자 엔티티에 대한 데이터 접근 인터페이스.
 */
interface UserRepository : JpaRepository<User, Long> {

    /**
     * 이메일로 사용자를 조회합니다.
     * @param email 조회할 이메일 주소
     * @return 해당 이메일을 가진 사용자 (없으면 empty)
     */
    fun findByEmail(email: String): Optional<User>

    /**
     * 이메일이 이미 존재하는지 확인합니다.
     * @param email 확인할 이메일 주소
     * @return 이메일이 존재하면 true
     */
    fun existsByEmail(email: String): Boolean
}
