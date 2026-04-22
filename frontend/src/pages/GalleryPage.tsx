import {useCallback, useEffect, useRef, useState} from 'react'
import {useNavigate} from 'react-router-dom'
import type {DiaryEntry, GalleryPhoto} from '../types'
import {fetchGalleryPhotos, fetchDiary} from '../api/diary'
import PhotoLightbox from '../components/PhotoLightbox'
import DetailModal from './DetailModal'
import ArrowBackIcon from '../assets/icon/arrow-back.svg?react'
import styles from './GalleryPage.module.scss'

type GridItem =
    | {kind: 'month'; label: string}
    | {kind: 'photo'; photo: GalleryPhoto; photoIndex: number}

/**
 * 갤러리 페이지 — 사용자가 일기에 첨부한 사진을 4열 그리드로 보여줍니다.
 * IntersectionObserver 기반 무한스크롤로 커서 페이지네이션을 처리합니다.
 * 월이 바뀔 때마다 연·월 구분선이 표시됩니다.
 */
function GalleryPage() {
    const navigate = useNavigate()
    const [photos, setPhotos] = useState<GalleryPhoto[]>([])
    const [loading, setLoading] = useState(false)
    const [lightboxIndex, setLightboxIndex] = useState<number | null>(null)
    const [detailEntry, setDetailEntry] = useState<DiaryEntry | null>(null)

    const loadingRef = useRef(false)
    const hasMoreRef = useRef(true)
    const nextCursorRef = useRef<string | null>(null)
    const photosRef = useRef<GalleryPhoto[]>([])
    const sentinelRef = useRef<HTMLDivElement>(null)

    /**
     * 다음 페이지 사진을 불러와 목록에 추가합니다.
     */
    const loadMore = useCallback(async () => {
        if (loadingRef.current || !hasMoreRef.current) return
        loadingRef.current = true
        setLoading(true)
        try {
            const {data} = await fetchGalleryPhotos(nextCursorRef.current ?? undefined)
            setPhotos((prev) => {
                const next = [...prev, ...data.content]
                photosRef.current = next
                return next
            })
            hasMoreRef.current = data.hasMore
            nextCursorRef.current = data.nextCursor
        } catch {
            // 에러는 api/client.ts 인터셉터에서 전역 처리
        } finally {
            loadingRef.current = false
            setLoading(false)
        }
    }, [])

    useEffect(() => {
        loadMore()
    }, [loadMore])

    useEffect(() => {
        const el = sentinelRef.current
        if (!el) return
        const observer = new IntersectionObserver(
            ([entry]) => { if (entry.isIntersecting) loadMore() },
            {rootMargin: '300px'},
        )
        observer.observe(el)
        return () => observer.disconnect()
    }, [loadMore])

    /**
     * 라이트박스에서 슬라이드가 변경될 때 호출됩니다.
     * 마지막 항목에 근접하면 다음 페이지를 미리 로드합니다.
     */
    const handleLightboxIndexChange = useCallback((index: number) => {
        setLightboxIndex(index)
        if (index >= photosRef.current.length - 3) loadMore()
    }, [loadMore])

    /**
     * 라이트박스의 "일기 보기" 버튼 클릭 시 해당 일기를 불러와 DetailModal을 엽니다.
     */
    const handleViewDiary = useCallback(async (diaryId: number) => {
        try {
            const {data} = await fetchDiary(diaryId)
            setLightboxIndex(null)
            setDetailEntry(data)
        } catch {
            // 에러는 인터셉터에서 처리
        }
    }, [])

    /**
     * 갤러리 사진 목록을 월 구분선과 함께 렌더링할 평탄화된 아이템 배열을 생성합니다.
     */
    const gridItems: GridItem[] = []
    let lastMonth = ''
    photos.forEach((photo, index) => {
        const [year, month] = photo.diaryDate.split('-')
        const monthKey = `${year}-${month}`
        if (monthKey !== lastMonth) {
            gridItems.push({kind: 'month', label: `${year}년 ${Number(month)}월`})
            lastMonth = monthKey
        }
        gridItems.push({kind: 'photo', photo, photoIndex: index})
    })

    const lightboxPhotos = photos.map((p) => ({id: p.id, imageUrl: p.imageUrl, displayOrder: 0}))
    const diaryIds = photos.map((p) => p.diaryId)

    return (
        <div className={styles.page}>
            <header className={styles.header}>
                <div className={styles.headerInner}>
                    <button className={styles.iconBtn} onClick={() => navigate(-1)} aria-label="뒤로 가기">
                        <ArrowBackIcon width={22} height={18}/>
                    </button>
                    <span className={styles.title}>갤러리</span>
                </div>
            </header>

            <main className={styles.scrollable}>
                <div className={styles.grid}>
                    {gridItems.map((item) =>
                        item.kind === 'month' ? (
                            <div key={`month-${item.label}`} className={styles.monthLabel}>
                                {item.label}
                            </div>
                        ) : (
                            <button
                                key={item.photo.id}
                                className={styles.photoCell}
                                onClick={() => setLightboxIndex(item.photoIndex)}
                                aria-label={`사진 보기`}
                            >
                                <img src={item.photo.imageUrl} alt="" loading="lazy"/>
                            </button>
                        ),
                    )}
                </div>
                {loading && <p className={styles.loadingText}>불러오는 중...</p>}
                {!loading && photos.length === 0 && (
                    <p className={styles.emptyText}>아직 사진이 없습니다.</p>
                )}
                <div ref={sentinelRef}/>
            </main>

            {lightboxIndex !== null && (
                <PhotoLightbox
                    photos={lightboxPhotos}
                    diaryIds={diaryIds}
                    initialIndex={lightboxIndex}
                    onClose={() => setLightboxIndex(null)}
                    onIndexChange={handleLightboxIndexChange}
                    onViewDiary={handleViewDiary}
                />
            )}

            {detailEntry && (
                <DetailModal
                    entry={detailEntry}
                    onClose={() => setDetailEntry(null)}
                    onDelete={(id: number) => {
                        setPhotos((prev) => {
                            const next = prev.filter((p) => p.diaryId !== id)
                            photosRef.current = next
                            return next
                        })
                        setDetailEntry(null)
                        setLightboxIndex(null)
                    }}
                    onUpdate={(updated: DiaryEntry) => {
                        setPhotos((prev) => {
                            const withoutOld = prev.filter((p) => p.diaryId !== updated.id)
                            const newPhotos: GalleryPhoto[] = updated.photos.map((p) => ({
                                id: p.id,
                                imageUrl: p.imageUrl,
                                diaryId: updated.id,
                                diaryDate: updated.diaryDate,
                            }))
                            // 기존 위치에 삽입: 첫 번째 일치 항목의 앞 인덱스를 찾아 삽입
                            const insertIdx = prev.findIndex((p) => p.diaryId === updated.id)
                            const next = insertIdx >= 0
                                ? [...withoutOld.slice(0, insertIdx), ...newPhotos, ...withoutOld.slice(insertIdx)]
                                : [...newPhotos, ...withoutOld]
                            photosRef.current = next
                            return next
                        })
                        setDetailEntry(updated)
                        setLightboxIndex(null)
                    }}
                />
            )}
        </div>
    )
}

export default GalleryPage
