import apiClient from './client'
import type {DiaryEntry, GalleryPhoto} from '../types'

export interface CreateDiaryParams {
    diaryDate: string   // yyyy-MM-dd
    content?: string
    photos?: File[]
}

export interface DiaryListResponse {
    content: DiaryEntry[]
    hasMore: boolean
    /** 다음 페이지 커서 (ISO yyyy-MM-dd). null이면 마지막 페이지 */
    nextCursor: string | null
}

export interface GalleryPhotoListResponse {
    content: GalleryPhoto[]
    hasMore: boolean
    /** 다음 페이지 커서. null이면 마지막 페이지. 형식: "yyyy-MM-dd_displayOrder" */
    nextCursor: string | null
}

/**
 * 새 일기를 생성합니다.
 * multipart/form-data로 날짜·본문·사진을 함께 전송합니다.
 * @param params 일기 날짜, 본문(선택), 사진 목록(선택)
 */
export const createDiary = (params: CreateDiaryParams) => {
    const form = new FormData()
    form.append('diaryDate', params.diaryDate)
    if (params.content) form.append('content', params.content)
    params.photos?.forEach((file) => form.append('photos', file))
    return apiClient.post('/diaries', form)
}

/**
 * 일기 목록을 커서 기반으로 조회합니다.
 * @param cursor 마지막으로 받은 일기의 diaryDate (없으면 첫 페이지)
 * @param size 한 번에 가져올 항목 수 (기본값 20)
 * @param query 본문 검색 키워드 (없으면 전체 조회)
 */
export const fetchDiaries = (cursor?: string, size = 20, query?: string) =>
    apiClient.get<DiaryListResponse>('/diaries', {params: {cursor, size, query}})

/**
 * 일기 단건을 조회합니다.
 * @param id 조회할 일기 ID
 */
export const fetchDiary = (id: number) =>
    apiClient.get<DiaryEntry>(`/diaries/${id}`)

/**
 * 갤러리용 사진 목록을 커서 기반으로 조회합니다. 일기 본문은 포함하지 않습니다.
 * @param cursor 마지막으로 받은 사진의 커서 문자열 (없으면 첫 페이지)
 * @param size 한 번에 가져올 항목 수 (기본값 40)
 */
export const fetchGalleryPhotos = (cursor?: string, size = 40) =>
    apiClient.get<GalleryPhotoListResponse>('/diaries/photos', {params: {cursor, size}})

/**
 * 특정 년월의 일기 목록을 캘린더용으로 조회합니다.
 * @param year 조회할 연도
 * @param month 조회할 월 (1~12)
 */
export const fetchCalendarDiaries = (year: number, month: number) =>
    apiClient.get<{content: DiaryEntry[]}>('/diaries/calendar', {params: {year, month}})

/**
 * 일기를 영구 삭제합니다.
 * @param id 삭제할 일기 ID
 */
export const deleteDiary = (id: number) => apiClient.delete(`/diaries/${id}`)

export interface UpdateDiaryParams {
    content?: string
    keepPhotoIds: number[]
    newPhotos?: File[]
}

/**
 * 일기를 수정합니다.
 * multipart/form-data로 본문·유지할 사진 ID·신규 사진 파일을 전송합니다.
 * @param id 수정할 일기 ID
 * @param params 본문(선택), 유지할 사진 ID 목록, 신규 사진(선택)
 */
export const updateDiary = (id: number, params: UpdateDiaryParams) => {
    const form = new FormData()
    if (params.content) form.append('content', params.content)
    params.keepPhotoIds.forEach((photoId) => form.append('keepPhotoIds', String(photoId)))
    params.newPhotos?.forEach((file) => form.append('photos', file))
    return apiClient.put<DiaryEntry>(`/diaries/${id}`, form)
}
