import {DayPicker, useDayPicker} from 'react-day-picker'
import {ko} from 'react-day-picker/locale'
import styles from './DatePickerModal.module.scss'

interface Props {
    selected: Date
    onSelect: (date: Date) => void
    onClose: () => void
}

/**
 * 커스텀 월 캡션 — 이전/다음 버튼을 caption_label 양옆에 배치합니다.
 */
function CustomMonthCaption({calendarMonth}: {calendarMonth: {date: Date}}) {
    const {goToMonth, previousMonth, nextMonth} = useDayPicker()
    const label = calendarMonth.date.toLocaleDateString('ko-KR', {year: 'numeric', month: 'long'})

    return (
        <div className={styles.captionRow}>
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
        </div>
    )
}

/**
 * 날짜 선택용 가운데 정렬 모달 컴포넌트.
 * 오버레이 클릭 또는 날짜 선택 시 닫힙니다.
 */
function DatePickerModal({selected, onSelect, onClose}: Props) {
    /**
     * 날짜를 선택하고 모달을 닫습니다.
     */
    const handleSelect = (date: Date | undefined) => {
        if (!date) return
        onSelect(date)
        onClose()
    }

    return (
        <div className={styles.overlay} onClick={onClose}>
            <div className={styles.sheet} onClick={(e) => e.stopPropagation()}>
                <DayPicker
                    mode="single"
                    locale={ko}
                    selected={selected}
                    onSelect={handleSelect}
                    className={styles.calendar}
                    disabled={{after: new Date()}}
                    components={{MonthCaption: CustomMonthCaption}}
                />
            </div>
        </div>
    )
}

export default DatePickerModal
