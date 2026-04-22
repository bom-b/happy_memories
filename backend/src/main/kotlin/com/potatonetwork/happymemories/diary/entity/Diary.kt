package com.potatonetwork.happymemories.diary.entity

import com.potatonetwork.happymemories.common.BaseEntity
import com.potatonetwork.happymemories.user.entity.User
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 일기 엔티티.
 * 사용자별 하루 한 편만 작성 가능합니다.
 */
@Entity
@Table(
    name = "diaries",
    indexes = [Index(name = "idx_diaries_user_date", columnList = "user_id, diary_date")]
)
class Diary(

    /** 이 일기를 작성한 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    /** 일기 작성 날짜 */
    @Column(nullable = false)
    var diaryDate: LocalDate,

    /** 일기 본문 */
    @Column(nullable = true, columnDefinition = "TEXT")
    var content: String = "",

) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    /** 첨부된 사진 목록 (최대 10장, displayOrder 오름차순) */
    @OneToMany(mappedBy = "diary", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    var photos: MutableList<DiaryPhoto> = mutableListOf()
}
