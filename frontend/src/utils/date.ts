const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토'] as const

/**
 * ISO 날짜 문자열(yyyy-MM-dd)에서 한국어 요일 문자를 반환합니다.
 * @param diaryDate ISO 형식 날짜 문자열
 */
export function getWeekday(diaryDate: string): string {
    const [y, m, d] = diaryDate.split('-').map(Number)
    return WEEKDAYS[new Date(y, m - 1, d).getDay()]
}
