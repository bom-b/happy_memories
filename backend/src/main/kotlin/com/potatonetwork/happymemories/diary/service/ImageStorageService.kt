package com.potatonetwork.happymemories.diary.service

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.webp.WebpWriter
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate

/**
 * 업로드된 이미지를 WebP로 변환하여 로컬 파일시스템(볼륨 마운트 경로)에 저장하는 서비스.
 * UPLOAD_PATH 환경 변수로 저장 경로를 지정하며 기본값은 /data/uploads 입니다.
 * 운영 환경에서는 Docker 볼륨 바인딩으로 해당 경로를 호스트에 영속화하고,
 * Nginx X-Accel-Redirect를 통해 인증된 사용자에게만 사진을 제공합니다.
 */
@Service
class ImageStorageService(
    @Value("\${app.upload.path}") private val uploadPath: String,
) {

    /**
     * MultipartFile을 WebP로 변환하여 날짜·사용자별 경로에 저장하고 접근 URL을 반환합니다.
     * 파일은 {uploadPath}/diary/{userId}/{YYYY}/{MM}/{DD}/{index}.webp 경로에 저장됩니다.
     * 반환 URL은 PhotoController가 처리하는 인증 보호 경로입니다.
     * @param file 업로드된 원본 이미지 파일
     * @param userId 일기 작성자 ID
     * @param diaryDate 일기 날짜
     * @param index 사진 순서 (1부터 시작)
     * @return 서블릿 상대 URL (예: /photos/diary/1/2024/01/15/1.webp)
     */
    fun store(file: MultipartFile, userId: Long, diaryDate: LocalDate, index: Int): String {
        val dir = Paths.get(
            uploadPath, "diary", userId.toString(),
            "%04d".format(diaryDate.year),
            "%02d".format(diaryDate.monthValue),
            "%02d".format(diaryDate.dayOfMonth),
        )
        Files.createDirectories(dir)

        val dest = dir.resolve("$index.webp")
        ImmutableImage.loader().fromBytes(file.bytes)
            .output(WebpWriter.DEFAULT, dest.toFile())

        return "/photos/diary/$userId/${"%04d".format(diaryDate.year)}/" +
                "${"%02d".format(diaryDate.monthValue)}/${"%02d".format(diaryDate.dayOfMonth)}/$index.webp"
    }
}
