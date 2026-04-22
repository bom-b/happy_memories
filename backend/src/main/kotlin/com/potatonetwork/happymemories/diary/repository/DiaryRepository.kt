package com.potatonetwork.happymemories.diary.repository

import com.potatonetwork.happymemories.diary.entity.Diary
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.Optional

/**
 * 일기 엔티티에 대한 데이터 접근 인터페이스.
 */
interface DiaryRepository : JpaRepository<Diary, Long> {

    /**
     * 특정 사용자의 특정 날짜 일기를 조회합니다 (삭제된 항목 제외).
     * @param userId 사용자 ID
     * @param diaryDate 조회할 날짜
     * @return 해당 날짜의 일기 (없으면 empty)
     */
    fun findByUserIdAndDiaryDate(userId: Long, diaryDate: LocalDate): Optional<Diary>

    /**
     * 특정 사용자의 특정 날짜에 유효한 일기가 존재하는지 확인합니다 (삭제된 항목 제외).
     * 새 일기 작성 전 중복 여부 검사에 사용합니다.
     * @param userId 사용자 ID
     * @param diaryDate 확인할 날짜
     * @return 이미 일기가 존재하면 true
     */
    fun existsByUserIdAndDiaryDate(userId: Long, diaryDate: LocalDate): Boolean

    /**
     * cursor(diaryDate) 이전 날짜의 일기를 최신순으로 조회합니다 (커서 기반 페이지네이션).
     * @param userId 사용자 ID
     * @param beforeDate 이 날짜보다 이전인 일기만 조회
     * @param pageable 페이지 크기 설정
     * @return 일기 Slice (hasNext로 다음 페이지 존재 여부 확인)
     */
    fun findByUserIdAndDiaryDateBeforeOrderByDiaryDateDesc(
        userId: Long, beforeDate: LocalDate, pageable: Pageable,
    ): Slice<Diary>

    /**
     * 특정 사용자의 일기 본문을 키워드로 검색합니다 (커서 기반 페이지네이션, 대소문자 무시).
     * @param userId 사용자 ID
     * @param before 이 날짜보다 이전인 일기만 조회
     * @param query 검색 키워드
     * @param pageable 페이지 크기 설정
     * @return 일기 Slice
     */
    @Query("SELECT d FROM Diary d WHERE d.user.id = :userId AND d.diaryDate < :before AND LOWER(d.content) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY d.diaryDate DESC")
    fun searchByContent(
        @Param("userId") userId: Long,
        @Param("before") before: LocalDate,
        @Param("query") query: String,
        pageable: Pageable,
    ): Slice<Diary>

    /**
     * 특정 사용자의 특정 기간 내 일기를 날짜 오름차순으로 조회합니다.
     * @param userId 사용자 ID
     * @param startDate 시작 날짜 (포함)
     * @param endDate 종료 날짜 (포함)
     * @return 해당 기간의 일기 목록
     */
    fun findByUserIdAndDiaryDateBetweenOrderByDiaryDateAsc(
        userId: Long, startDate: LocalDate, endDate: LocalDate,
    ): List<Diary>
}
