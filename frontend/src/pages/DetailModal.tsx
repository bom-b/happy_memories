import {useEffect, useState} from 'react'
import {createPortal} from 'react-dom'
import type {DiaryEntry} from '../types'
import {getWeekday} from '../utils/date'
import {deleteDiary} from '../api/diary'
import EditDiaryModal from './EditDiaryModal'
import DragScrollContainer from '../components/DragScrollContainer'
import PhotoLightbox from '../components/PhotoLightbox'
import ArrowBackIcon from '../assets/icon/arrow-back.svg?react'
import PencilIcon from '../assets/icon/pencil.svg?react'
import DeleteIcon from '../assets/icon/delete.svg?react'
import styles from './DetailModal.module.scss'

interface Props {
    entry: DiaryEntry
    onClose: () => void
    onDelete: (id: number) => void
    onUpdate: (updated: DiaryEntry) => void
}

/**
 * 일기 상세 모달 컴포넌트 — entry와 onClose 콜백을 props로 전달받아 전체 화면 모달로 표시합니다.
 */
function DetailModal({entry, onClose, onDelete, onUpdate}: Props) {
    const [currentEntry, setCurrentEntry] = useState(entry)
    const [year, month, day] = currentEntry.diaryDate.split('-').map(Number)
    const [showEdit, setShowEdit] = useState(false)
    const [lightboxIndex, setLightboxIndex] = useState<number | null>(null)

    /** 모달이 열려 있는 동안 #root를 숨겨 body 스크롤이 모달 콘텐츠 기준으로 동작하게 합니다. */
    useEffect(() => {
        const root = document.getElementById('root')
        const prevDisplay = root?.style.display ?? ''
        const prevScroll = window.scrollY
        if (root) root.style.display = 'none'
        window.scrollTo(0, 0)
        return () => {
            if (root) root.style.display = prevDisplay
            window.scrollTo(0, prevScroll)
        }
    }, [])

    /**
     * 일기를 삭제하고 모달을 닫습니다.
     */
    const handleDelete = async () => {
        const ok = confirm('일기를 삭제하시겠습니까?');
        if (!ok) return;

        await deleteDiary(currentEntry.id)
        onDelete(currentEntry.id)
        onClose()
    }

    return (
        <>
            {createPortal(
                <div className={styles.overlay}>
                    <div className={styles.modal} style={{display: showEdit ? 'none' : undefined}}>
                        <header className={styles.header}>
                            <div className={styles.headerInner}>
                                <button className={styles.iconBtn} onClick={onClose} aria-label="닫기">
                                    <ArrowBackIcon width={22} height={18}/>
                                </button>
                                <div className={styles.headerActions}>
                                    <button className={styles.iconBtn} aria-label="수정" onClick={() => setShowEdit(true)}>
                                        <PencilIcon width={22} height={22}/>
                                    </button>
                                    <button className={styles.iconBtn} aria-label="삭제" onClick={handleDelete}>
                                        <DeleteIcon width={20} height={22}/>
                                    </button>
                                </div>
                            </div>
                        </header>

                        <main className={styles.scrollable}>
                            <div className={styles.content}>
                                <h1 className={styles.date}>
                                    {year}년 {month}월 {day}일 {getWeekday(currentEntry.diaryDate)}요일
                                </h1>
                                <div className={styles.text} dangerouslySetInnerHTML={{__html: currentEntry.content}}/>
                                {currentEntry.photos.length > 0 && (
                                    <DragScrollContainer className={styles.images}>
                                        {currentEntry.photos.map((photo, idx) => (
                                            <img
                                                key={photo.id}
                                                src={photo.imageUrl}
                                                alt={`사진 ${photo.displayOrder}`}
                                                className={styles.image}
                                                onClick={() => setLightboxIndex(idx)}
                                            />
                                        ))}
                                    </DragScrollContainer>
                                )}
                            </div>
                        </main>
                    </div>
                </div>,
                document.body,
            )}
            {showEdit && (
                <EditDiaryModal
                    entry={currentEntry}
                    onClose={() => setShowEdit(false)}
                    onSave={(updated) => {
                        setCurrentEntry(updated)
                        onUpdate(updated)
                        setShowEdit(false)
                    }}
                />
            )}
            {lightboxIndex !== null && (
                <PhotoLightbox
                    photos={currentEntry.photos}
                    initialIndex={lightboxIndex}
                    onClose={() => setLightboxIndex(null)}
                    zIndex={10001}
                />
            )}
        </>
    )
}

export default DetailModal