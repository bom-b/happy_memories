import {useEffect, useState} from 'react'
import {useNavigate} from 'react-router-dom'
import {DayPicker, useDayPicker} from 'react-day-picker'
import {ko} from 'react-day-picker/locale'
import type {DayButtonProps} from 'react-day-picker'
import type {DiaryEntry} from '../types'
import {fetchCalendarDiaries} from '../api/diary'
import {getWeekday} from '../utils/date'
import DetailModal from './DetailModal'
import DragScrollContainer from '../components/DragScrollContainer'
import ArrowBackIcon from '../assets/icon/arrow-back.svg?react'
import styles from './CalendarPage.module.scss'

/**
 * 일기가 있는 날에 점을 표시하는 커스텀 DayButton 컴포넌트.
 */
function DayButton({className, day, modifiers, children, ...props}: DayButtonProps) {
    return (
        <button className={className} {...props}>
            {children}
            {modifiers.hasDiary && <span className={styles.dot}/>}
        </button>
    )
}

/**
 * «‹ 년월 ›» 형태로 이전/다음 달·연도를 이동하는 커스텀 캡션 컴포넌트.
 */
function CustomMonthCaption({calendarMonth}: {calendarMonth: {date: Date}}) {
    const {goToMonth, previousMonth, nextMonth} = useDayPicker()
    const today = new Date()
    const year = calendarMonth.date.getFullYear()
    const mo = calendarMonth.date.getMonth()
    const canGoPrevYear = year > 2020
    const canGoNextYear = new Date(year + 1, mo, 1) <= today
    const label = calendarMonth.date.toLocaleDateString('ko-KR', {year: 'numeric', month: 'long'})

    return (
        <div className={styles.caption}>
            <button
                className={styles.navBtn}
                onClick={() => canGoPrevYear && goToMonth(new Date(year - 1, mo, 1))}
                disabled={!canGoPrevYear}
                aria-label="이전 연도"
            >
                «
            </button>
            <button
                className={styles.navBtn}
                onClick={() => previousMonth && goToMonth(previousMonth)}
                disabled={!previousMonth}
                aria-label="이전 달"
            >
                ‹
            </button>
            <span className={styles.captionLabel}>{label}</span>
            <button
                className={styles.navBtn}
                onClick={() => nextMonth && goToMonth(nextMonth)}
                disabled={!nextMonth}
                aria-label="다음 달"
            >
                ›
            </button>
            <button
                className={styles.navBtn}
                onClick={() => canGoNextYear && goToMonth(new Date(year + 1, mo, 1))}
                disabled={!canGoNextYear}
                aria-label="다음 연도"
            >
                »
            </button>
        </div>
    )
}

/**
 * 날짜 객체를 yyyy-MM-dd 문자열로 변환합니다.
 */
function toDateString(date: Date): string {
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const d = String(date.getDate()).padStart(2, '0')
    return `${y}-${m}-${d}`
}

/**
 * 캘린더 페이지 — 월별 달력과 선택된 날짜의 일기 미리보기를 보여줍니다.
 */
function CalendarPage() {
    const navigate = useNavigate()
    const today = new Date()

    const [month, setMonth] = useState(today)
    const [diaries, setDiaries] = useState<DiaryEntry[]>([])
    const [selectedDate, setSelectedDate] = useState<Date>(today)
    const [showDetail, setShowDetail] = useState(false)

    /** 월이 바뀌면 해당 월 일기 목록을 새로 조회합니다. */
    useEffect(() => {
        fetchCalendarDiaries(month.getFullYear(), month.getMonth() + 1)
            .then(({data}) => setDiaries(data.content))
            .catch(() => {})
    }, [month])

    const datesWithDiary = diaries.map((d) => {
        const [y, mo, dy] = d.diaryDate.split('-').map(Number)
        return new Date(y, mo - 1, dy)
    })

    const selectedEntry = diaries.find((d) => d.diaryDate === toDateString(selectedDate))
    const [year, selMonth, selDay] = toDateString(selectedDate).split('-').map(Number)

    return (
        <div className={styles.page}>
            <header className={styles.header}>
                <div className={styles.headerInner}>
                    <button className={styles.iconBtn} onClick={() => navigate('/feed')} aria-label="뒤로가기">
                        <ArrowBackIcon width={22} height={18}/>
                    </button>
                    <span className={styles.title}>캘린더</span>
                </div>
            </header>

            <section className={styles.calendarSection}>
                <DayPicker
                    mode="single"
                    locale={ko}
                    month={month}
                    onMonthChange={setMonth}
                    selected={selectedDate}
                    onSelect={(date) => date && setSelectedDate(date)}
                    disabled={{after: today}}
                    className={styles.calendar}
                    modifiers={{hasDiary: datesWithDiary}}
                    components={{DayButton, MonthCaption: CustomMonthCaption}}
                />
            </section>

            <section className={styles.entrySection}>
                {selectedEntry ? (
                    <div className={styles.entryCard} onClick={() => setShowDetail(true)}>
                        <h2 className={styles.entryDate}>
                            {year}년 {selMonth}월 {selDay}일 {getWeekday(toDateString(selectedDate))}요일
                        </h2>
                        <div
                            className={styles.entryContent}
                            dangerouslySetInnerHTML={{__html: selectedEntry.content}}
                        />
                        {selectedEntry.photos.length > 0 && (
                            <DragScrollContainer className={styles.thumbnails}>
                                {selectedEntry.photos.map((photo) => (
                                    <img
                                        key={photo.id}
                                        src={photo.imageUrl}
                                        alt=""
                                        className={styles.thumbnail}
                                    />
                                ))}
                            </DragScrollContainer>
                        )}
                    </div>
                ) : (
                    <div className={styles.emptyState}>
                        <p className={styles.emptyText}>작성한 일기가 없습니다</p>
                    </div>
                )}
            </section>

            {showDetail && selectedEntry && (
                <DetailModal
                    entry={selectedEntry}
                    onClose={() => setShowDetail(false)}
                    onDelete={(id) => {
                        setDiaries((prev) => prev.filter((e) => e.id !== id))
                        setShowDetail(false)
                    }}
                    onUpdate={(updated) => {
                        setDiaries((prev) => prev.map((e) => (e.id === updated.id ? updated : e)))
                    }}
                />
            )}
        </div>
    )
}

export default CalendarPage
