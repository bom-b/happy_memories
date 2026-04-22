import {useEffect, useRef, useState} from 'react'
import {createPortal} from 'react-dom'
import {useEditor, EditorContent} from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'
import Placeholder from '@tiptap/extension-placeholder'
import type {DiaryEntry, DiaryPhoto} from '../types'
import {updateDiary} from '../api/diary'
import {getWeekday} from '../utils/date'
import ArrowBackIcon from '../assets/icon/arrow-back.svg?react'
import GalleryIcon from '../assets/icon/gallery1.svg?react'
import PlusIcon from '../assets/icon/plus.svg?react'
import DragScrollContainer from '../components/DragScrollContainer'
import styles from './EditDiaryModal.module.scss'

interface Props {
    entry: DiaryEntry
    onClose: () => void
    onSave: (updated: DiaryEntry) => void
}

/**
 * 일기 수정 모달 컴포넌트.
 * 기존 entry를 초기값으로 에디터와 사진 목록을 채우고, 저장 시 PUT API를 호출합니다.
 * 날짜는 수정 불가하며 표시만 합니다.
 */
function EditDiaryModal({entry, onClose, onSave}: Props) {
    const [year, month, day] = entry.diaryDate.split('-').map(Number)
    const [keepPhotos, setKeepPhotos] = useState<DiaryPhoto[]>(entry.photos)
    const [newPhotos, setNewPhotos] = useState<File[]>([])
    const [saving, setSaving] = useState(false)
    const fileInputRef = useRef<HTMLInputElement>(null)

    const editor = useEditor({
        extensions: [
            StarterKit,
            Placeholder.configure({placeholder: '내용을 입력하세요...'}),
        ],
        content: entry.content,
        editorProps: {
            attributes: {class: styles.editor},
        },
    })

    /** 모달이 열리면 맨 위로 스크롤하고, 닫히면 상세 모달 상단으로 복원합니다. */
    useEffect(() => {
        window.scrollTo(0, 0)
        return () => { window.scrollTo(0, 0) }
    }, [])

    /**
     * 파일 선택 다이얼로그를 엽니다.
     */
    const handleGalleryClick = () => fileInputRef.current?.click()

    /**
     * 선택된 파일을 newPhotos 목록에 추가합니다. 전체 합산 최대 10장까지 허용합니다.
     */
    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const selected = Array.from(e.target.files ?? [])
        setNewPhotos((prev) => {
            const merged = [...prev, ...selected]
            const remaining = 10 - keepPhotos.length
            return merged.slice(0, remaining)
        })
        e.target.value = ''
    }

    /**
     * 기존 사진을 유지 목록에서 제거합니다.
     */
    const handleRemoveKeep = (id: number) => {
        setKeepPhotos((prev) => prev.filter((p) => p.id !== id))
    }

    /**
     * 신규 추가 사진을 목록에서 제거합니다.
     */
    const handleRemoveNew = (idx: number) => {
        setNewPhotos((prev) => prev.filter((_, i) => i !== idx))
    }

    /**
     * 수정 내용을 저장하고 onSave 콜백을 호출합니다.
     */
    const handleSave = async () => {
        if (saving) return
        setSaving(true)
        try {
            const {data} = await updateDiary(entry.id, {
                content: editor?.isEmpty ? undefined : editor?.getHTML(),
                keepPhotoIds: keepPhotos.map((p) => p.id),
                newPhotos,
            })
            onSave(data)
        } finally {
            setSaving(false)
        }
    }

    const totalCount = keepPhotos.length + newPhotos.length

    return createPortal(
        <div className={styles.overlay}>
            <div className={styles.modal}>
                <header className={styles.header}>
                    <button className={styles.backBtn} onClick={onClose} aria-label="수정 취소">
                        <ArrowBackIcon width={22} height={18}/>
                    </button>
                    <button className={styles.saveBtn} onClick={handleSave} disabled={saving}>
                        저장
                    </button>
                </header>

                <main className={styles.scrollable}>
                    <div className={styles.content}>
                        <p className={styles.dateLabel}>
                            {year}년 {month}월 {day}일 {getWeekday(entry.diaryDate)}요일
                        </p>

                        <div onKeyDown={(e) => { if (e.key === 'Tab') e.preventDefault() }}>
                            <EditorContent editor={editor}/>
                        </div>

                        <div className={styles.photoRow}>
                            {totalCount === 0 ? (
                                <button className={styles.galleryBtn} onClick={handleGalleryClick} aria-label="사진 첨부">
                                    <GalleryIcon width={36} height={36}/>
                                </button>
                            ) : (
                                <DragScrollContainer className={styles.photoList}>
                                    {keepPhotos.map((photo) => (
                                        <div key={photo.id} className={styles.photoThumbWrapper}>
                                            <img
                                                src={photo.imageUrl}
                                                alt={`사진 ${photo.displayOrder}`}
                                                className={styles.photoThumb}
                                            />
                                            <button
                                                className={styles.removePhotoBtn}
                                                onClick={() => handleRemoveKeep(photo.id)}
                                                aria-label="사진 삭제"
                                            >
                                                ×
                                            </button>
                                        </div>
                                    ))}
                                    {newPhotos.map((file, idx) => (
                                        <div key={idx} className={styles.photoThumbWrapper}>
                                            <img
                                                src={URL.createObjectURL(file)}
                                                alt={`새 사진 ${idx + 1}`}
                                                className={styles.photoThumb}
                                            />
                                            <button
                                                className={styles.removePhotoBtn}
                                                onClick={() => handleRemoveNew(idx)}
                                                aria-label="사진 삭제"
                                            >
                                                ×
                                            </button>
                                        </div>
                                    ))}
                                    {totalCount < 10 && (
                                        <button className={styles.addPhotoBtn} onClick={handleGalleryClick} aria-label="사진 추가">
                                            <PlusIcon width={22} height={22}/>
                                        </button>
                                    )}
                                </DragScrollContainer>
                            )}
                        </div>
                    </div>
                </main>

                <input
                    ref={fileInputRef}
                    type="file"
                    accept="image/*"
                    multiple
                    hidden
                    onChange={handleFileChange}
                />
            </div>
        </div>,
        document.body,
    )
}

export default EditDiaryModal
