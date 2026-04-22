package com.potatonetwork.happymemories.user.entity

import com.potatonetwork.happymemories.common.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 디바이스별 리프레시 토큰 엔티티.
 * 로그인 시 발급되며, 로그아웃 또는 만료 시 무효화됩니다.
 * 한 사용자가 여러 디바이스에 동시에 로그인할 수 있도록 사용자당 여러 개가 존재할 수 있습니다.
 */
@Entity
@Table(
    name = "refresh_tokens",
    indexes = [Index(name = "idx_refresh_tokens_token", columnList = "token", unique = true)]
)
class RefreshToken(

    /** 토큰 소유 사용자 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    /** UUID 기반으로 생성된 불투명 토큰 값 */
    @Column(nullable = false, unique = true, length = 512)
    val token: String,

    /** 토큰 만료 일시 — 액세스 토큰 갱신 시 함께 연장됨 */
    @Column(nullable = false)
    var expiresAt: LocalDateTime,

) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}
