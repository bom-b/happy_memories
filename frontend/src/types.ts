/** 일기에 첨부된 사진 타입 */
export interface DiaryPhoto {
    id: number
    imageUrl: string
    displayOrder: number
}

/** 일기 항목 타입 */
export interface DiaryEntry {
    id: number
    /** ISO 날짜 문자열 (yyyy-MM-dd) */
    diaryDate: string
    /** Tiptap이 생성한 HTML 본문 */
    content: string
    photos: DiaryPhoto[]
}

/** 갤러리 사진 항목 타입 — 일기 본문은 포함하지 않습니다. */
export interface GalleryPhoto {
    id: number
    imageUrl: string
    diaryId: number
    /** ISO 날짜 문자열 (yyyy-MM-dd), 월 구분선 표시용 */
    diaryDate: string
}
