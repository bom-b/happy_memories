package com.potatonetwork.happymemories.diary.controller

import com.potatonetwork.happymemories.diary.dto.CalendarDiaryListResponse
import com.potatonetwork.happymemories.diary.dto.CreateDiaryRequest
import com.potatonetwork.happymemories.diary.dto.CreateDiaryResponse
import com.potatonetwork.happymemories.diary.dto.DiaryListResponse
import com.potatonetwork.happymemories.diary.dto.GalleryPhotoListResponse
import com.potatonetwork.happymemories.diary.service.DiaryService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

/**
 * 일기 관련 REST API 컨트롤러.
 */
@RestController
@RequestMapping("/diaries")
class DiaryController(private val diaryService: DiaryService) {

    /**
     * 인증된 사용자의 일기 목록을 커서 기반으로 반환합니다.
     * cursor가 없으면 첫 페이지, 있으면 해당 날짜 이전 항목을 반환합니다.
     * @param userId 인증 필터가 주입한 사용자 ID
     * @param cursor 마지막으로 받은 일기의 diaryDate (ISO yyyy-MM-dd, 없으면 첫 페이지)
     * @param size 한 번에 가져올 항목 수 (기본값 20)
     * @return 200 OK + 커서 페이지 응답 DTO
     */
    @GetMapping
    fun list(
        @AuthenticationPrincipal userId: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) cursor: LocalDate?,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) query: String?,
    ): DiaryListResponse = diaryService.list(userId.toLong(), cursor, size, query)

    /**
     * 새 일기를 생성합니다.
     * multipart/form-data로 일기 날짜·본문과 사진 파일을 함께 전송합니다.
     * @param userId 인증 필터가 주입한 사용자 ID 문자열
     * @param diaryDate 일기 날짜 (ISO 형식: yyyy-MM-dd)
     * @param content 일기 본문 (선택)
     * @param photos 첨부 사진 목록 (선택, 최대 10장)
     * @return 201 Created + 생성된 일기 응답 DTO
     */
    @PostMapping(consumes = ["multipart/form-data"])
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @AuthenticationPrincipal userId: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) diaryDate: LocalDate,
        @RequestParam(required = false) content: String?,
        @RequestParam(required = false) photos: List<MultipartFile>?,
    ): CreateDiaryResponse =
        diaryService.create(
            userId = userId.toLong(),
            request = CreateDiaryRequest(diaryDate = diaryDate, content = content),
            photos = photos.orEmpty().filter { !it.isEmpty },
        )

    /**
     * 일기를 수정합니다.
     * multipart/form-data로 본문·유지할 사진 ID·신규 사진 파일을 전송합니다.
     * @param userId 인증 필터가 주입한 사용자 ID
     * @param id 수정할 일기 ID
     * @param content 새 본문 (선택)
     * @param keepPhotoIds 유지할 기존 사진 ID 목록 (선택)
     * @param photos 새로 추가할 사진 목록 (선택)
     * @return 200 OK + 수정된 일기 응답 DTO
     */
    @PutMapping("/{id}", consumes = ["multipart/form-data"])
    fun update(
        @AuthenticationPrincipal userId: String,
        @PathVariable id: Long,
        @RequestParam(required = false) content: String?,
        @RequestParam(required = false) keepPhotoIds: List<Long>?,
        @RequestParam(required = false) photos: List<MultipartFile>?,
    ): CreateDiaryResponse =
        diaryService.update(
            userId = userId.toLong(),
            diaryId = id,
            content = content,
            keepPhotoIds = keepPhotoIds.orEmpty(),
            newPhotos = photos.orEmpty().filter { !it.isEmpty },
        )

    /**
     * 일기를 영구 삭제(Hard Delete)합니다.
     * @param userId 인증 필터가 주입한 사용자 ID
     * @param id 삭제할 일기 ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @AuthenticationPrincipal userId: String,
        @PathVariable id: Long,
    ) = diaryService.delete(userId.toLong(), id)

    /**
     * 일기 단건을 조회합니다.
     * @param userId 인증 필터가 주입한 사용자 ID
     * @param id 조회할 일기 ID
     * @return 200 OK + 일기 응답 DTO
     */
    @GetMapping("/{id}")
    fun get(
        @AuthenticationPrincipal userId: String,
        @PathVariable id: Long,
    ): CreateDiaryResponse = diaryService.findById(userId.toLong(), id)

    /**
     * 인증된 사용자의 사진 목록을 갤러리용으로 커서 기반 반환합니다.
     * 일기 본문은 포함하지 않으며, (diaryDate DESC, displayOrder ASC) 순으로 반환합니다.
     * @param userId 인증 필터가 주입한 사용자 ID
     * @param cursor 마지막으로 받은 사진의 커서 문자열 (형식: "yyyy-MM-dd_displayOrder")
     * @param size 한 번에 가져올 항목 수 (기본값 40)
     * @return 200 OK + 갤러리 사진 커서 페이지 응답 DTO
     */
    @GetMapping("/photos")
    fun listPhotos(
        @AuthenticationPrincipal userId: String,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "40") size: Int,
    ): GalleryPhotoListResponse = diaryService.listPhotos(userId.toLong(), cursor, size)

    /**
     * 특정 년월의 일기 목록을 캘린더용으로 반환합니다.
     * @param userId 인증 필터가 주입한 사용자 ID
     * @param year 조회할 연도
     * @param month 조회할 월 (1~12)
     * @return 200 OK + 해당 월의 일기 목록 응답 DTO
     */
    @GetMapping("/calendar")
    fun listByMonth(
        @AuthenticationPrincipal userId: String,
        @RequestParam year: Int,
        @RequestParam month: Int,
    ): CalendarDiaryListResponse = diaryService.listByMonth(userId.toLong(), year, month)
}
