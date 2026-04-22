package com.potatonetwork.happymemories.diary.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.nio.file.Files
import java.nio.file.Paths

/**
 * 사진 보호 전송 컨트롤러.
 * JWT로 인증된 본인의 사진만 접근할 수 있도록 소유권을 검증한 뒤,
 * Nginx X-Accel-Redirect 헤더를 통해 파일을 내부 전달합니다.
 * Nginx 없이 직접 접근하는 개발 환경에서는 Spring이 직접 파일을 응답합니다.
 */
@RestController
class PhotoController(
    @Value("\${app.upload.path}") private val uploadPath: String,
) {

    /**
     * 인증된 사용자의 사진을 반환합니다.
     * URL 경로에서 소유자 ID를 추출해 로그인 사용자와 일치하는지 확인합니다.
     * 경로 형식: /photos/diary/{ownerId}/{YYYY}/{MM}/{DD}/{index}.webp
     * @param userId JWT에서 추출한 인증 사용자 ID
     * @param request 서블릿 요청 (경로 추출용)
     * @return 이미지 응답 (Nginx 환경: 빈 바디 + X-Accel-Redirect, 직접 접근: 파일 바디)
     */
    @GetMapping("/photos/**")
    fun getPhoto(
        @AuthenticationPrincipal userId: String,
        request: HttpServletRequest,
    ): ResponseEntity<Resource> {
        // servletPath: /photos/diary/{ownerId}/{YYYY}/{MM}/{DD}/{index}.webp
        val servletPath = request.servletPath
        val segments = servletPath.split("/")

        // 경로 구조 검증 및 소유자 ID 추출
        val ownerId = segments.getOrNull(3)?.toLongOrNull()
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 사진 경로입니다.")

        if (userId.toLong() != ownerId) throw ResponseStatusException(HttpStatus.FORBIDDEN)

        // /photos 접두사를 제거한 내부 경로: /diary/{ownerId}/{YYYY}/{MM}/{DD}/{index}.webp
        val internalPath = servletPath.removePrefix("/photos")

        // 경로 순회(Path Traversal) 공격 방지
        val baseDir = Paths.get(uploadPath).toAbsolutePath().normalize()
        val targetFile = baseDir.resolve(internalPath.removePrefix("/")).normalize()
        if (!targetFile.startsWith(baseDir)) throw ResponseStatusException(HttpStatus.FORBIDDEN)

        if (!Files.exists(targetFile)) throw ResponseStatusException(HttpStatus.NOT_FOUND)

        // Nginx X-Accel-Redirect: Nginx가 /internal/ 위치에서 파일을 직접 전송
        // 개발 환경(Nginx 없음): Spring이 파일 바디를 직접 응답
        return ResponseEntity.ok()
            .header("X-Accel-Redirect", "/internal$internalPath")
            .contentType(MediaType.parseMediaType("image/webp"))
            .body(FileSystemResource(targetFile.toFile()))
    }
}
