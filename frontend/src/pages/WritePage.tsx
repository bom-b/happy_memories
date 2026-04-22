import {useRef, useState} from 'react'
import {useNavigate} from 'react-router-dom'
import {useEditor, EditorContent} from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'
import Placeholder from '@tiptap/extension-placeholder'
import GalleryIcon from '../assets/icon/gallery1.svg?react'
import PlusIcon from '../assets/icon/plus.svg?react'
import ArrowBackIcon from '../assets/icon/arrow-back.svg?react'
import {createDiary} from '../api/diary'
import DatePickerModal from '../components/DatePickerModal'
import DragScrollContainer from '../components/DragScrollContainer'
import styles from './WritePage.module.scss'

/**
 * 날짜를 'yyyy년 M월 d일' 형식으로 반환합니다.
 */
function formatDateLabel(date: Date): string {
    return `${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일`
}

/**
 * Date 객체를 ISO 'yyyy-MM-dd' 형식 문자열로 변환합니다.
 */
function toIsoDate(date: Date): string {
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const d = String(date.getDate()).padStart(2, '0')
    return `${y}-${m}-${d}`
}

/**
 * 새 일기를 작성하는 페이지 컴포넌트.
 * 오늘 날짜가 기본값으로 표시되며 본문과 사진을 첨부하여 저장합니다.
 */
function WritePage() {
    const navigate = useNavigate()
    const [selectedDate, setSelectedDate] = useState(new Date())
    const [showDatePicker, setShowDatePicker] = useState(false)
    const [photos, setPhotos] = useState<File[]>([])
    const [saving, setSaving] = useState(false)
    const fileInputRef = useRef<HTMLInputElement>(null)

    const editor = useEditor({
        extensions: [
            StarterKit,
            Placeholder.configure({placeholder: '오늘 하루는 어떠셨나요?'}),
        ],
        editorProps: {
            attributes: {class: styles.editor},
        },
    })

    /**
     * 파일 선택 다이얼로그를 엽니다.
     */
    const handleGalleryClick = () => fileInputRef.current?.click()

    /**
     * 선택된 파일을 photos 목록에 추가합니다. 최대 10장까지 허용합니다.
     */
    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const selected = Array.from(e.target.files ?? [])
        setPhotos((prev) => {
            const merged = [...prev, ...selected]
            return merged.slice(0, 10)
        })
        e.target.value = ''
    }

    /**
     * 지정한 인덱스의 사진을 목록에서 제거합니다.
     */
    const handleRemovePhoto = (idx: number) => {
        setPhotos((prev) => prev.filter((_, i) => i !== idx))
    }

    /**
     * 사진 추가 버튼(+)을 눌러 파일 선택 다이얼로그를 추가로 엽니다.
     */
    const handleAddMore = () => fileInputRef.current?.click()

    /**
     * 일기를 저장하고 피드 화면으로 이동합니다.
     */
    const handleSave = async () => {
        if (saving) return
        setSaving(true)
        try {
            await createDiary({
                diaryDate: toIsoDate(selectedDate),
                content: editor?.isEmpty ? undefined : editor?.getHTML(),
                photos,
            })
            navigate('/feed')
        } finally {
            setSaving(false)
        }
    }

    return (
        <div className={styles.page}>
            <header className={styles.header}>
                <button className={styles.iconBtn} onClick={() => navigate(-1)} aria-label="뒤로가기">
                    <ArrowBackIcon width={22} height={18}/>
                </button>
                <button className={styles.saveBtn} onClick={handleSave} disabled={saving}>
                    저장
                </button>
            </header>

            <main className={styles.scrollable}>
                <div className={styles.content}>
                    <button
                        className={styles.dateLabel}
                        onClick={() => setShowDatePicker(true)}
                        aria-label="날짜 선택"
                    >
                        {formatDateLabel(selectedDate)}
                    </button>

                    <div onKeyDown={(e) => { if (e.key === 'Tab') e.preventDefault() }}>
                        <EditorContent editor={editor} />
                    </div>

                    <div className={styles.photoRow}>
                        {photos.length === 0 ? (
                            <button className={styles.galleryBtn} onClick={handleGalleryClick} aria-label="사진 첨부">
                                <GalleryIcon width={36} height={36}/>
                            </button>
                        ) : (
                            <DragScrollContainer className={styles.photoList}>
                                {photos.map((file, idx) => (
                                    <div key={idx} className={styles.photoThumbWrapper}>
                                        <img
                                            src={URL.createObjectURL(file)}
                                            alt={`첨부 사진 ${idx + 1}`}
                                            className={styles.photoThumb}
                                        />
                                        <button
                                            className={styles.removePhotoBtn}
                                            onClick={() => handleRemovePhoto(idx)}
                                            aria-label={`사진 ${idx + 1} 삭제`}
                                        >
                                            ×
                                        </button>
                                    </div>
                                ))}
                                {photos.length < 10 && (
                                    <button className={styles.addPhotoBtn} onClick={handleAddMore} aria-label="사진 추가">
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

            {showDatePicker && (
                <DatePickerModal
                    selected={selectedDate}
                    onSelect={setSelectedDate}
                    onClose={() => setShowDatePicker(false)}
                />
            )}
        </div>
    )
}

export default WritePage
