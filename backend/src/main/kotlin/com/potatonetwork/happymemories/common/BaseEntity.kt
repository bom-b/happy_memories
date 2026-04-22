package com.potatonetwork.happymemories.common

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

/**
 * 생성일시, 수정일시를 공통으로 관리하는 기본 엔티티 클래스.
 * 모든 주요 엔티티는 이 클래스를 상속받아 Auditing 기능을 사용합니다.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {

    /** 엔티티 생성일시 (최초 저장 시 자동 기록) */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    /** 엔티티 최종 수정일시 (저장 또는 수정 시 자동 갱신) */
    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
}
