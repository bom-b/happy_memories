package com.potatonetwork.happymemories.diary.service

import com.potatonetwork.happymemories.diary.dto.CreateDiaryRequest
import com.potatonetwork.happymemories.diary.entity.Diary
import com.potatonetwork.happymemories.diary.repository.DiaryPhotoRepository
import com.potatonetwork.happymemories.diary.repository.DiaryRepository
import com.potatonetwork.happymemories.user.entity.User
import com.potatonetwork.happymemories.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class DiaryServiceTest {

    @Mock private lateinit var diaryRepository: DiaryRepository
    @Mock private lateinit var diaryPhotoRepository: DiaryPhotoRepository
    @Mock private lateinit var userRepository: UserRepository
    @Mock private lateinit var imageStorageService: ImageStorageService

    @InjectMocks private lateinit var diaryService: DiaryService

    /** 테스트용 User 객체를 생성 */
    private fun makeUser(id: Long = 1L) = User(
        "test@example.com",
        "pw",
        "테스터"
    ).also { it.id = id }

    /** 테스트용 Diary 객체를 생성 */
    private fun makeDiary(
        id: Long = 1L,
        userId: Long = 1L,
        date: LocalDate = LocalDate.of(2024, 1, 15)
    ) = Diary(
        user = makeUser(userId),
        diaryDate = date,
        content = "테스트 내용"
    ).also { it.id = id }

    @Test
    fun `사진이 11장이면 400 Bad Request를 던진다`() {
        // 11개의 가짜 파일 목록을 만듭니다. DB 호출 없이 즉시 예외가 발생해야 합니다.
        val photos = (1..11).map { MockMultipartFile("photo", "img.jpg", "image/jpeg", ByteArray(1)) }

        val ex = assertThrows<ResponseStatusException> {
            diaryService.create(1L, CreateDiaryRequest(LocalDate.of(2024, 1, 15)), photos)
        }
        assertThat(ex.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `같은 날짜에 이미 일기가 있으면 409 Conflict를 던진다`() {
        val date = LocalDate.of(2024, 1, 15)
        `when`(userRepository.findById(1L)).thenReturn(Optional.of(makeUser()))
        `when`(diaryRepository.existsByUserIdAndDiaryDate(1L, date)).thenReturn(true)

        val ex = assertThrows<ResponseStatusException> {
            diaryService.create(1L, CreateDiaryRequest(date), emptyList())
        }
        assertThat(ex.statusCode).isEqualTo(HttpStatus.CONFLICT)
    }

    @Test
    fun `정상적인 일기 생성 요청은 저장 후 응답 DTO를 반환한다`() {
        val date = LocalDate.of(2024, 1, 15)
        val diary = makeDiary(date = date).also { it.content = "내용" }
        `when`(userRepository.findById(1L)).thenReturn(Optional.of(makeUser()))
        `when`(diaryRepository.existsByUserIdAndDiaryDate(1L, date)).thenReturn(false)
        `when`(diaryRepository.save(any(Diary::class.java))).thenReturn(diary)

        val result = diaryService.create(1L, CreateDiaryRequest(date, "내용"), emptyList())

        assertThat(result.diaryDate).isEqualTo(date)
        assertThat(result.content).isEqualTo("내용")
        verify(diaryRepository).save(any(Diary::class.java))
    }

    @Test
    fun `다른 사용자의 일기를 수정하면 403 Forbidden을 던진다`() {
        // 일기 소유자 id=99, 요청자 id=1 → 403
        `when`(diaryRepository.findById(1L)).thenReturn(Optional.of(makeDiary(userId = 99L)))

        val ex = assertThrows<ResponseStatusException> {
            diaryService.update(1L, 1L, null, emptyList(), emptyList())
        }
        assertThat(ex.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `유지 사진과 신규 사진 합계가 10장을 초과하면 400 Bad Request를 던진다`() {
        `when`(diaryRepository.findById(1L)).thenReturn(Optional.of(makeDiary()))
        val keepPhotoIds = (1L..8L).toList()   // 8장 유지
        val newPhotos = (1..3).map { MockMultipartFile("photo", "img.jpg", "image/jpeg", ByteArray(1)) } // 3장 추가

        val ex = assertThrows<ResponseStatusException> {
            diaryService.update(1L, 1L, null, keepPhotoIds, newPhotos)
        }
        assertThat(ex.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `다른 사용자의 일기를 삭제하면 403 Forbidden을 던진다`() {
        `when`(diaryRepository.findById(1L)).thenReturn(Optional.of(makeDiary(userId = 99L)))

        val ex = assertThrows<ResponseStatusException> {
            diaryService.delete(1L, 1L)
        }
        assertThat(ex.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `소유자가 일기를 삭제하면 정상적으로 처리된다`() {
        val diary = makeDiary(userId = 1L)
        `when`(diaryRepository.findById(1L)).thenReturn(Optional.of(diary))

        diaryService.delete(1L, 1L)

        verify(diaryRepository).delete(diary)
    }

    @Test
    fun `다른 사용자의 일기를 조회하면 403 Forbidden을 던진다`() {
        `when`(diaryRepository.findById(1L)).thenReturn(Optional.of(makeDiary(userId = 99L)))

        val ex = assertThrows<ResponseStatusException> {
            diaryService.findById(1L, 1L)
        }
        assertThat(ex.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }
}
