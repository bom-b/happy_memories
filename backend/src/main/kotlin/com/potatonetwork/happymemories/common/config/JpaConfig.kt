package com.potatonetwork.happymemories.common.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

/**
 * JPA Auditing을 활성화하는 설정 클래스.
 * BaseEntity의 createdAt, updatedAt 필드가 자동으로 기록됩니다.
 */
@Configuration
@EnableJpaAuditing
class JpaConfig
