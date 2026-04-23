import {useEffect, useLayoutEffect, useRef} from 'react'
import {modalStack} from './modalStack'

/**
 * 모달 컴포넌트에서 뒤로가기(하드웨어 버튼, iOS 스와이프, 브라우저 백)를
 * URL 이동 대신 모달 닫기로 처리하는 훅입니다.
 * 마운트 시 modalStack에 등록하고, 언마운트 시 제거합니다.
 */
export function useModalHistory(onClose: () => void): void {
    const onCloseRef = useRef(onClose)

    useLayoutEffect(() => {
        onCloseRef.current = onClose
    })

    useEffect(() => {
        const handler = () => onCloseRef.current()
        modalStack.push(handler)
        return () => modalStack.remove(handler)
    }, [])
}
