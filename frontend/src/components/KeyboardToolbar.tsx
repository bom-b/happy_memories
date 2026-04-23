import {useEffect, useState} from 'react'
import {Capacitor} from '@capacitor/core'
import {Keyboard} from '@capacitor/keyboard'
import type {Editor} from '@tiptap/react'
import IndentIcon from '../assets/icon/indent.svg?react'
import OutdentIcon from '../assets/icon/outdent.svg?react'
import styles from './KeyboardToolbar.module.scss'

interface Props {
    editor: Editor | null
}

/**
 * Capacitor 네이티브 환경에서 키보드가 열릴 때 하단에 표시되는 들여쓰기/내어쓰기 툴바 컴포넌트.
 * 웹 환경에서는 렌더링하지 않습니다.
 */
function KeyboardToolbar({editor}: Props) {
    const [visible, setVisible] = useState(false)

    /**
     * Keyboard 플러그인 이벤트로 키보드 표시 여부를 감지합니다.
     */
    useEffect(() => {
        if (!Capacitor.isNativePlatform()) return
        const showListener = Keyboard.addListener('keyboardWillShow', () => setVisible(true))
        const hideListener = Keyboard.addListener('keyboardWillHide', () => setVisible(false))
        return () => {
            showListener.then(h => h.remove())
            hideListener.then(h => h.remove())
        }
    }, [])

    if (!visible) return null

    return (
        <div className={styles.toolbar}>
            <button
                className={styles.iconBtn}
                onPointerDown={(e) => {
                    e.preventDefault()
                    editor?.chain().focus().liftListItem('listItem').run()
                }}
                aria-label="내어쓰기"
            >
                <OutdentIcon width={22} height={22}/>
            </button>
            <button
                className={styles.iconBtn}
                onPointerDown={(e) => {
                    e.preventDefault()
                    editor?.chain().focus().sinkListItem('listItem').run()
                }}
                aria-label="들여쓰기"
            >
                <IndentIcon width={22} height={22}/>
            </button>
        </div>
    )
}

export default KeyboardToolbar
