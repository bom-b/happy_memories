package com.potatonetwork.happymemories.user.entity

import com.potatonetwork.happymemories.common.BaseEntity
import jakarta.persistence.*

/**
 * 사용자 엔티티.
 * 이메일과 BCrypt로 암호화된 비밀번호로 인증을 처리하며, 표시 이름을 보유합니다.
 */
@Entity
@Table(
    name = "users",
    indexes = [Index(name = "idx_users_email", columnList = "email", unique = true)]
)
class User(

    /** 로그인에 사용되는 이메일 주소 (중복 불가) */
    @Column(nullable = false, unique = true, length = 255)
    var email: String,

    /** BCrypt로 암호화된 비밀번호 */
    @Column(nullable = false)
    var password: String,

    /** 사용자 표시 이름 */
    @Column(nullable = false, length = 50)
    var name: String,

) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
}
