package com.potatonetwork.happymemories.diary.service

import com.potatonetwork.happymemories.diary.dto.CalendarDiaryListResponse
import com.potatonetwork.happymemories.diary.dto.CreateDiaryRequest
import com.potatonetwork.happymemories.diary.dto.CreateDiaryResponse
import com.potatonetwork.happymemories.diary.dto.DiaryListResponse
import com.potatonetwork.happymemories.diary.dto.DiaryPhotoResponse
import com.potatonetwork.happymemories.diary.dto.GalleryPhotoListResponse
import com.potatonetwork.happymemories.diary.dto.GalleryPhotoResponse
import com.potatonetwork.happymemories.diary.entity.Diary
import com.potatonetwork.happymemories.diary.entity.DiaryPhoto
import com.potatonetwork.happymemories.diary.repository.DiaryPhotoRepository
import com.potatonetwork.happymemories.diary.repository.DiaryRepository
import com.potatonetwork.happymemories.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

/**
 * 일기 관련 비즈니스 로직을 처리하는 서비스.
 */
@Service
class DiaryService(
    private val diaryRepository: DiaryRepository,
    private val diaryPhotoRepository: DiaryPhotoRepository,
    private val userRepository: UserRepository,
    private val imageStorageService: ImageStorageService,
) {

    /**
     * 인증된 사용자의 일기 목록을 커서 기반으로 최신순 조회합니다.
     * cursor가 없으면 첫 페이지를 반환하고, cursor(diaryDate)가 있으면 해당 날짜 이전 항목을 반환합니다.
     * query가 있으면 본문 키워드 검색 결과를 반환합니다.
     * @param userId 인증된 사용자 ID
     * @param cursor 마지막으로 받은 일기의 diaryDate (없으면 첫 페이지)
     * @param size 한 번에 가져올 항목 수
     * @param query 본문 검색 키워드 (없으면 전체 조회)
     * @return 커서 페이지 응답 DTO
     */
    @Transactional(readOnly = true)
    fun list(userId: Long, cursor: LocalDate?, size: Int, query: String?): DiaryListResponse {
        val before = cursor ?: LocalDate.now().plusDays(1)
        val slice = if (query.isNullOrBlank()) {
            diaryRepository.findByUserIdAndDiaryDateBeforeOrderByDiaryDateDesc(
                userId, before, PageRequest.of(0, size),
            )
        } else {
            diaryRepository.searchByContent(userId, before, query.trim(), PageRequest.of(0, size))
        }
        return DiaryListResponse(
            content = slice.content.map { diary ->
                CreateDiaryResponse(
                    id = diary.id,
                    diaryDate = diary.diaryDate,
                    content = diary.content,
                    photos = diary.photos.map { DiaryPhotoResponse(it.id, it.imageUrl, it.displayOrder) },
                )
            },
            hasMore = slice.hasNext(),
            nextCursor = if (slice.hasNext()) slice.content.last().diaryDate.toString() else null,
        )
    }

    /**
     * 새 일기를 생성합니다. 같은 날짜에 이미 일기가 있으면 409를 반환합니다.
     * 첨부 사진이 있으면 WebP로 변환하여 저장합니다. 최대 10장까지 허용합니다.
     * @param userId 인증된 사용자 ID
     * @param request 일기 날짜·본문 DTO
     * @param photos 첨부 이미지 목록 (없으면 빈 배열)
     * @return 생성된 일기의 응답 DTO
     */
    @Transactional
    fun create(userId: Long, request: CreateDiaryRequest, photos: List<MultipartFile>): CreateDiaryResponse {
        if (photos.size > 10) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "사진은 최대 10장까지 첨부할 수 있습니다.")

        val user = userRepository.findById(userId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }

        if (diaryRepository.existsByUserIdAndDiaryDate(userId, request.diaryDate)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "해당 날짜에 이미 일기가 존재합니다.")
        }

        val diary = Diary(
            user = user,
            diaryDate = request.diaryDate,
            content = request.content ?: "",
        )

        photos.forEachIndexed { index, file ->
            val url = imageStorageService.store(file, userId, request.diaryDate, index + 1)
            diary.photos.add(DiaryPhoto(diary = diary, imageUrl = url, displayOrder = index + 1))
        }

        val saved = diaryRepository.save(diary)

        return CreateDiaryResponse(
            id = saved.id,
            diaryDate = saved.diaryDate,
            content = saved.content,
            photos = saved.photos.map { DiaryPhotoResponse(it.id, it.imageUrl, it.displayOrder) },
        )
    }

    /**
     * 일기를 수정합니다. 소유자가 아닌 경우 403을 반환합니다.
     * keepPhotoIds에 없는 기존 사진을 제거하고 신규 사진을 추가합니다.
     * 기존 사진의 displayOrder를 재색인하고, 신규 사진을 그 뒤에 이어붙입니다.
     * @param userId 인증된 사용자 ID
     * @param diaryId 수정할 일기 ID
     * @param content 새 본문 (null이면 빈 문자열)
     * @param keepPhotoIds 유지할 기존 사진 ID 목록
     * @param newPhotos 새로 추가할 사진 목록
     * @return 수정된 일기의 응답 DTO
     */
    @Transactional
    fun update(userId: Long, diaryId: Long, content: String?, keepPhotoIds: List<Long>, newPhotos: List<MultipartFile>): CreateDiaryResponse {
        val diary = diaryRepository.findById(diaryId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        if (diary.user.id != userId) throw ResponseStatusException(HttpStatus.FORBIDDEN)
        if (keepPhotoIds.size + newPhotos.size > 10)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "사진은 최대 10장까지 첨부할 수 있습니다.")

        val keepSet = keepPhotoIds.toSet()
        diary.photos.retainAll { it.id in keepSet }

        diary.photos.sortedBy { it.displayOrder }.forEachIndexed { i, photo ->
            photo.displayOrder = i + 1
        }

        // 기존 파일명과 충돌하지 않도록 kept 사진 URL에서 인덱스 최댓값을 구해 그 이후 번호 사용
        val maxIndex = diary.photos.mapNotNull {
            it.imageUrl.substringAfterLast('/').substringBefore('.').toIntOrNull()
        }.maxOrNull() ?: 0
        val keptCount = diary.photos.size

        newPhotos.forEachIndexed { i, file ->
            val url = imageStorageService.store(file, userId, diary.diaryDate, maxIndex + i + 1)
            diary.photos.add(DiaryPhoto(diary = diary, imageUrl = url, displayOrder = keptCount + i + 1))
        }

        diary.content = content ?: ""

        return CreateDiaryResponse(
            id = diary.id,
            diaryDate = diary.diaryDate,
            content = diary.content,
            photos = diary.photos.sortedBy { it.displayOrder }.map { DiaryPhotoResponse(it.id, it.imageUrl, it.displayOrder) },
        )
    }

    /**
     * 일기를 영구 삭제(Hard Delete)합니다. 소유자가 아닌 경우 403을 반환합니다.
     * @param userId 인증된 사용자 ID
     * @param diaryId 삭제할 일기 ID
     */
    @Transactional
    fun delete(userId: Long, diaryId: Long) {
        val diary = diaryRepository.findById(diaryId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        if (diary.user.id != userId) throw ResponseStatusException(HttpStatus.FORBIDDEN)
        diaryRepository.delete(diary)
    }

    /**
     * 일기 단건을 조회합니다. 소유자가 아닌 경우 403을 반환합니다.
     * @param userId 인증된 사용자 ID
     * @param diaryId 조회할 일기 ID
     * @return 일기 응답 DTO
     */
    @Transactional(readOnly = true)
    fun findById(userId: Long, diaryId: Long): CreateDiaryResponse {
        val diary = diaryRepository.findById(diaryId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        if (diary.user.id != userId) throw ResponseStatusException(HttpStatus.FORBIDDEN)
        return CreateDiaryResponse(
            id = diary.id,
            diaryDate = diary.diaryDate,
            content = diary.content,
            photos = diary.photos.map { DiaryPhotoResponse(it.id, it.imageUrl, it.displayOrder) },
        )
    }

    /**
     * 사용자의 사진 목록을 갤러리용으로 커서 기반 조회합니다.
     * 일기 본문은 포함하지 않으며 (diaryDate DESC, displayOrder ASC) 순으로 반환합니다.
     * cursor 형식: "yyyy-MM-dd_displayOrder" (없으면 첫 페이지)
     * @param userId 인증된 사용자 ID
     * @param cursor 마지막으로 받은 사진의 커서 문자열
     * @param size 한 번에 가져올 항목 수
     * @return 갤러리 사진 커서 페이지 응답 DTO
     */
    @Transactional(readOnly = true)
    fun listPhotos(userId: Long, cursor: String?, size: Int): GalleryPhotoListResponse {
        val (cursorDate, cursorOrder) = if (cursor != null) {
            val parts = cursor.split("_")
            LocalDate.parse(parts[0]) to parts[1].toInt()
        } else {
            LocalDate.now().plusDays(1) to 0
        }

        val slice = diaryPhotoRepository.findGalleryPhotos(
            userId, cursorDate, cursorOrder, PageRequest.of(0, size),
        )

        return GalleryPhotoListResponse(
            content = slice.content.map { photo ->
                GalleryPhotoResponse(
                    id = photo.id,
                    imageUrl = photo.imageUrl,
                    diaryId = photo.diary.id,
                    diaryDate = photo.diary.diaryDate,
                )
            },
            hasMore = slice.hasNext(),
            nextCursor = if (slice.hasNext()) {
                val last = slice.content.last()
                "${last.diary.diaryDate}_${last.displayOrder}"
            } else null,
        )
    }

    /**
     * 특정 년월의 일기 목록을 캘린더용으로 조회합니다.
     * @param userId 인증된 사용자 ID
     * @param year 조회할 연도
     * @param month 조회할 월 (1~12)
     * @return 해당 월의 일기 목록 응답 DTO
     */
    @Transactional(readOnly = true)
    fun listByMonth(userId: Long, year: Int, month: Int): CalendarDiaryListResponse {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.withDayOfMonth(startDate.lengthOfMonth())
        val diaries = diaryRepository.findByUserIdAndDiaryDateBetweenOrderByDiaryDateAsc(userId, startDate, endDate)
        return CalendarDiaryListResponse(
            content = diaries.map { diary ->
                CreateDiaryResponse(
                    id = diary.id,
                    diaryDate = diary.diaryDate,
                    content = diary.content,
                    photos = diary.photos.map { DiaryPhotoResponse(it.id, it.imageUrl, it.displayOrder) },
                )
            },
        )
    }
}
