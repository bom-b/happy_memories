package com.potatonetwork.happymemories.diary.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 일기에 첨부된 사진 엔티티.
 * 한 일기에 최대 10장까지 첨부 가능하며, displayOrder로 표시 순서를 관리합니다.
 */
@Entity
@Table(
    name = "diary_photos",
    indexes = [Index(name = "idx_diary_photos_diary_id", columnList = "diary_id")]
)
class DiaryPhoto(

    /** 이 사진이 첨부된 일기 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = false)
    var diary: Diary,

    /** 저장된 사진의 URL 또는 파일 경로 */
    @Column(nullable = false, length = 500)
    var imageUrl: String,

    /** 사진 표시 순서 (1부터 시작) */
    @Column(nullable = false)
    var displayOrder: Int,

) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    /** 사진 업로드 일시 */
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
}
