import {useMemo, useState} from 'react'
import Lightbox from 'yet-another-react-lightbox'
import 'yet-another-react-lightbox/styles.css'
import type {DiaryPhoto} from '../types'
import styles from './PhotoLightbox.module.scss'

interface Props {
    photos: DiaryPhoto[]
    initialIndex: number
    onClose: () => void
    /** 제공하면 각 슬라이드 하단에 "일기 보기" 버튼이 표시됩니다. */
    onViewDiary?: (diaryId: number) => void
    /** photos와 평행한 배열로 각 사진에 대응하는 일기 ID를 제공합니다. */
    diaryIds?: number[]
    /** 슬라이드가 변경될 때마다 호출됩니다 (무한스크롤 연동용). */
    onIndexChange?: (index: number) => void
    /** 라이트박스 루트 요소의 z-index. 기본값은 라이브러리 기본값(9999)을 따릅니다. */
    zIndex?: number
}

/**
 * 사진 전체 화면 뷰어 모달.
 * yet-another-react-lightbox 기반으로 드래그/스와이프, 이전·다음 버튼을 지원합니다.
 * diaryIds와 onViewDiary를 함께 제공하면 각 슬라이드에 "일기 보기" 버튼이 표시됩니다.
 */
function PhotoLightbox({photos, initialIndex, onClose, onViewDiary, diaryIds, onIndexChange, zIndex}: Props) {
    const [currentIndex, setCurrentIndex] = useState(initialIndex)
    // 라이트박스가 열리기 전(마운트 시점)의 스크롤바 너비를 캡처합니다.
    // 라이트박스는 열릴 때 body에 overflow:hidden을 걸어 스크롤바를 제거하고
    // 뷰포트 너비가 넓어지므로, left: 50%가 콘텐츠 중심과 어긋납니다.
    // 이를 scrollbarWidth / 2 만큼 왼쪽으로 보정합니다.
    const scrollbarOffset = useMemo(
        () => (window.innerWidth - document.documentElement.clientWidth) / 2,
        [],
    )

    const slides = photos.map((photo, i) => ({
        src: photo.imageUrl,
        diaryId: diaryIds?.[i],
    }))

    const handleView = ({index}: {index: number}) => {
        setCurrentIndex(index)
        onIndexChange?.(index)
    }

    return (
        <Lightbox
            open
            close={onClose}
            slides={slides}
            index={currentIndex}
            className={styles.lightbox}
            styles={{
                root: {
                    ...(zIndex !== undefined ? {zIndex} : {}),
                    right: 'auto',
                    left: `calc(50% - ${scrollbarOffset}px)`,
                    transform: 'translateX(-50%)',
                },
                container: {backgroundColor: 'rgba(0, 0, 0, 0.92)'},
            }}
            carousel={{finite: true}}
            on={{view: handleView}}
            render={onViewDiary ? {
                controls: () => {
                    const diaryId = diaryIds?.[currentIndex]
                    if (!diaryId) return null
                    return (
                        <div className={styles.viewDiaryBtnWrapper}>
                            <button className={styles.viewDiaryBtn} onClick={() => onViewDiary(diaryId)}>
                                일기 보기
                            </button>
                        </div>
                    )
                },
            } : undefined}
        />
    )
}

export default PhotoLightbox
