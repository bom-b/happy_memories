package com.potatonetwork.happymemories.diary.dto

import java.time.LocalDate

/** 일기 생성 요청 DTO (multipart/form-data 필드 바인딩) */
data class CreateDiaryRequest(
    val diaryDate: LocalDate,
    val content: String? = null,
)

/** 일기 수정 요청 DTO — 유지할 사진 ID 목록과 신규 사진은 컨트롤러에서 @RequestParam으로 별도 처리 */
data class UpdateDiaryRequest(
    val content: String? = null,
    val keepPhotoIds: List<Long> = emptyList(),
)

/** 첨부 사진 응답 DTO */
data class DiaryPhotoResponse(
    val id: Long,
    val imageUrl: String,
    val displayOrder: Int,
)

/** 일기 생성 응답 DTO */
data class CreateDiaryResponse(
    val id: Long,
    val diaryDate: LocalDate,
    val content: String,
    val photos: List<DiaryPhotoResponse>,
)

/** 일기 목록 커서 페이지 응답 DTO */
data class DiaryListResponse(
    val content: List<CreateDiaryResponse>,
    val hasMore: Boolean,
    /** 다음 페이지 커서 (ISO yyyy-MM-dd). null이면 마지막 페이지 */
    val nextCursor: String?,
)

/** 갤러리 사진 항목 응답 DTO — 일기 본문은 포함하지 않습니다. */
data class GalleryPhotoResponse(
    val id: Long,
    val imageUrl: String,
    val diaryId: Long,
    /** 일기 날짜 (월 구분선 표시용) */
    val diaryDate: LocalDate,
)

/** 갤러리 사진 목록 커서 페이지 응답 DTO */
data class GalleryPhotoListResponse(
    val content: List<GalleryPhotoResponse>,
    val hasMore: Boolean,
    /** 다음 페이지 커서. null이면 마지막 페이지. 형식: "yyyy-MM-dd_displayOrder" */
    val nextCursor: String?,
)

/** 캘린더용 월별 일기 목록 응답 DTO */
data class CalendarDiaryListResponse(
    val content: List<CreateDiaryResponse>,
)
