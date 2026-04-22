import {useCallback, useEffect, useRef, useState} from 'react'
import {useNavigate} from 'react-router-dom'
import type {DiaryEntry} from '../types'
import {fetchDiaries} from '../api/diary'
import {getWeekday} from '../utils/date'
import DetailModal from './DetailModal.tsx'
import DragScrollContainer from '../components/DragScrollContainer'
import SearchIcon from '../assets/icon/magnifying-glass.svg?react'
import SettingIcon from '../assets/icon/setting.svg?react'
import CalendarIcon from '../assets/icon/calendar.svg?react'
import GalleryIcon from '../assets/icon/gallery2.svg?react'
import PlusIcon from '../assets/icon/plus.svg?react'
import styles from './FeedPage.module.scss'

/**
 * 일기 피드 목록을 보여주는 메인 화면 컴포넌트.
 * IntersectionObserver 기반 무한스크롤로 커서 페이지네이션을 처리합니다.
 */
function FeedPage() {
    const navigate = useNavigate()
    const [entries, setEntries] = useState<DiaryEntry[]>([])
    const [loading, setLoading] = useState(false)
    const [isSearchActive, setIsSearchActive] = useState(false)
    const [searchInput, setSearchInput] = useState('')
    const [selectedEntry, setSelectedEntry] = useState<DiaryEntry | null>(null)

    // 리렌더링 없이 최신 상태를 유지하는 ref (무한스크롤 guard 용도)
    const loadingRef = useRef(false)
    const hasMoreRef = useRef(true)
    const nextCursorRef = useRef<string | null>(null)
    const queryRef = useRef('')
    const sentinelRef = useRef<HTMLDivElement>(null)
    const [navHidden, setNavHidden] = useState(false)
    const lastScrollY = useRef(0)

    useEffect(() => {
        const handleScroll = () => {
            const current = window.scrollY
            setNavHidden(current > lastScrollY.current && current > 10)
            lastScrollY.current = current
        }
        window.addEventListener('scroll', handleScroll, {passive: true})
        return () => window.removeEventListener('scroll', handleScroll)
    }, [])

    /**
     * 다음 페이지 일기를 불러와 목록에 추가합니다.
     * loadingRef로 중복 호출을 방지하고, hasMoreRef로 마지막 페이지 여부를 확인합니다.
     */
    const loadMore = useCallback(async () => {
        if (loadingRef.current || !hasMoreRef.current) return
        loadingRef.current = true
        setLoading(true)
        try {
            const {data} = await fetchDiaries(nextCursorRef.current ?? undefined, 20, queryRef.current || undefined)
            setEntries((prev) => [...prev, ...data.content])
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

    /**
     * sentinel 요소가 뷰포트에 진입하면 다음 페이지를 로드합니다.
     */
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
     * 검색어를 확정하고 목록을 초기화한 뒤 첫 페이지를 다시 로드합니다.
     */
    const handleSearch = () => {
        queryRef.current = searchInput.trim()
        setEntries([])
        hasMoreRef.current = true
        nextCursorRef.current = null
        loadMore()
    }

    /**
     * 검색 모드를 닫고, 검색 중이었다면 전체 목록으로 복원합니다.
     */
    const handleSearchClose = () => {
        setIsSearchActive(false)
        setSearchInput('')
        if (queryRef.current !== '') {
            queryRef.current = ''
            setEntries([])
            hasMoreRef.current = true
            nextCursorRef.current = null
            loadMore()
        }
    }

    /**
     * 일기 항목 클릭 시 상세 모달을 엽니다.
     */
    const handleEntryClick = (entry: DiaryEntry) => {
        setSelectedEntry(entry)
    }

    return (
        <div className={styles.page}>
            <header className={styles.header}>
                <div className={styles.headerInner}>
                    {isSearchActive ? (
                        <div className={styles.searchBar}>
                            <button className={styles.iconBtnBlack} aria-label="검색 닫기" onClick={handleSearchClose}>
                                <SearchIcon width={22} height={22}/>
                            </button>
                            <input
                                className={styles.searchInput}
                                value={searchInput}
                                onChange={(e) => setSearchInput(e.target.value)}
                                onKeyDown={(e) => { if (e.key === 'Enter') handleSearch() }}
                                placeholder="일기 검색..."
                                autoFocus
                            />
                            <button className={styles.searchSubmitBtn} onClick={handleSearch}>
                                검색
                            </button>
                        </div>
                    ) : (
                        <div className={styles.headerActions}>
                            <button className={styles.iconBtnBlack} aria-label="검색" onClick={() => setIsSearchActive(true)}>
                                <SearchIcon width={22} height={22}/>
                            </button>
                            <button className={styles.iconBtnBlack} aria-label="설정" onClick={() => navigate('/settings')}>
                                <SettingIcon width={22} height={22}/>
                            </button>
                        </div>
                    )}
                </div>
            </header>

            <main className={styles.scrollable}>
                <div className={styles.content}>
                    <ul className={styles.entryList}>
                        {entries.map((entry, idx) => {
                            const [year, month, day] = entry.diaryDate.split('-').map(Number)
                            const prevYear = idx > 0 ? Number(entries[idx - 1].diaryDate.slice(0, 4)) : null
                            const showYearLabel = prevYear === null || year !== prevYear
                            return (
                                <li
                                    key={entry.id}
                                    className={styles.entryItem}
                                    onClick={() => handleEntryClick(entry)}
                                >
                                    {showYearLabel && <p className={styles.yearLabel}>{year}년</p>}
                                    <h2 className={styles.entryDate}>
                                        {month}월 {day}일 {getWeekday(entry.diaryDate)}요일
                                    </h2>
                                    <div className={styles.entryContent} dangerouslySetInnerHTML={{__html: entry.content}}/>
                                    {entry.photos.length > 0 && (
                                        <DragScrollContainer className={styles.thumbnails}>
                                            {entry.photos.map((photo) => (
                                                <img
                                                    key={photo.id}
                                                    src={photo.imageUrl}
                                                    alt=""
                                                    className={styles.thumbnail}
                                                />
                                            ))}
                                        </DragScrollContainer>
                                    )}
                                </li>
                            )
                        })}
                    </ul>
                    {loading && <p className={styles.loadingText}>불러오는 중...</p>}
                    <div ref={sentinelRef}/>
                </div>
            </main>

            <nav className={`${styles.bottomNav}${navHidden ? ` ${styles.bottomNavHidden}` : ''}`}>
                <div className={styles.bottomNavInner}>
                    <button className={styles.iconBtnGray} aria-label="캘린더" onClick={() => navigate('/calendar')}>
                        <CalendarIcon width={26} height={26}/>
                    </button>
                    <button className={styles.addBtn} aria-label="새 일기 작성" onClick={() => navigate('/feed/write')}>
                        <PlusIcon width={22} height={22}/>
                    </button>
                    <button className={styles.iconBtnGray} aria-label="갤러리" onClick={() => navigate('/gallery')}>
                        <GalleryIcon width={26} height={26}/>
                    </button>
                </div>
            </nav>

            {selectedEntry && (
                <DetailModal
                    entry={selectedEntry}
                    onClose={() => setSelectedEntry(null)}
                    onDelete={(id) => setEntries((prev) => prev.filter((e) => e.id !== id))}
                    onUpdate={(updated) => setEntries((prev) => prev.map((e) => e.id === updated.id ? updated : e))}
                />
            )}
        </div>
    )
}

export default FeedPage
