package com.potatonetwork.happymemories.diary.repository

import com.potatonetwork.happymemories.diary.entity.DiaryPhoto
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

/**
 * 일기 사진 엔티티에 대한 데이터 접근 인터페이스.
 */
interface DiaryPhotoRepository : JpaRepository<DiaryPhoto, Long> {

    /**
     * 특정 일기에 첨부된 사진 수를 반환합니다.
     * 사진 추가 전 최대 10장 제한 검사에 사용합니다.
     * @param diaryId 일기 ID
     * @return 현재 첨부된 사진 수
     */
    fun countByDiaryId(diaryId: Long): Long

    /**
     * 특정 일기의 사진을 표시 순서에 따라 조회합니다.
     * @param diaryId 일기 ID
     * @return 표시 순서(displayOrder) 오름차순으로 정렬된 사진 목록
     */
    fun findAllByDiaryIdOrderByDisplayOrder(diaryId: Long): List<DiaryPhoto>

    /**
     * 사용자의 사진을 갤러리용으로 커서 기반 페이지네이션으로 조회합니다.
     * (diaryDate DESC, displayOrder ASC) 순으로 정렬하며, 커서 이후 항목만 반환합니다.
     * Diary를 JOIN FETCH하여 N+1 문제를 방지합니다.
     * @param userId 사용자 ID
     * @param cursorDate 커서 날짜 (이 날짜보다 이전이거나, 같은 날짜면 cursorOrder 이후 항목)
     * @param cursorOrder 커서 표시 순서
     * @param pageable 페이지 크기 설정
     * @return 사진 Slice
     */
    @Query("""
        SELECT p FROM DiaryPhoto p JOIN FETCH p.diary d
        WHERE d.user.id = :userId
          AND (d.diaryDate < :cursorDate OR (d.diaryDate = :cursorDate AND p.displayOrder > :cursorOrder))
        ORDER BY d.diaryDate DESC, p.displayOrder ASC
    """)
    fun findGalleryPhotos(
        @Param("userId") userId: Long,
        @Param("cursorDate") cursorDate: LocalDate,
        @Param("cursorOrder") cursorOrder: Int,
        pageable: Pageable,
    ): Slice<DiaryPhoto>
}
