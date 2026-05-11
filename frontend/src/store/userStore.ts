import {create} from 'zustand'

export interface User {
    name: string
    email: string
}

interface UserStore {
    user: User | null
    /** 앱 최초 마운트 시 /me 응답 전까지 true, 응답 후 false로 전환됩니다. */
    initializing: boolean
    /** 로그인 또는 /me 조회 후 사용자 정보를 설정합니다. */
    setUser: (user: User) => void
    /** 로그아웃 또는 인증 만료 시 사용자 정보를 초기화합니다. */
    clearUser: () => void
    /** /me 조회 완료(성공·실패 무관) 후 initializing 상태를 갱신합니다. */
    setInitializing: (v: boolean) => void
}

const useUserStore = create<UserStore>((set) => ({
    user: null,
    initializing: true,
    setUser: (user) => set({user}),
    clearUser: () => set({user: null}),
    setInitializing: (v) => set({initializing: v}),
}))

export default useUserStore
