const stack: Array<() => void> = []

/**
 * 모달 뒤로가기 처리를 위한 핸들러 스택입니다.
 * 모달이 열릴 때 핸들러를 push하고, 닫힐 때 remove합니다.
 * 뒤로가기 이벤트 발생 시 pop으로 최상단 핸들러를 호출합니다.
 */
export const modalStack = {
    /**
     * 핸들러를 스택에 추가하고 히스토리 엔트리를 삽입합니다.
     */
    push(handler: () => void): void {
        stack.push(handler)
        window.history.pushState({modal: true, depth: stack.length}, '')
    },

    /**
     * 핸들러를 스택에서 제거합니다 (프로그래매틱 닫기 시 사용).
     */
    remove(handler: () => void): void {
        const idx = stack.lastIndexOf(handler)
        if (idx !== -1) stack.splice(idx, 1)
    },

    /**
     * 최상단 핸들러를 꺼내 호출합니다.
     */
    pop(): void {
        const handler = stack.pop()
        if (handler) handler()
    },

    /**
     * 현재 스택에 등록된 핸들러 수를 반환합니다.
     */
    size(): number {
        return stack.length
    },
}
