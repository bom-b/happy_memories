import {create} from 'zustand'

export interface User {
    name: string
    email: string
}

interface UserStore {
    user: User | null
    /** 로그인 또는 /me 조회 후 사용자 정보를 설정합니다. */
    setUser: (user: User) => void
    /** 로그아웃 또는 인증 만료 시 사용자 정보를 초기화합니다. */
    clearUser: () => void
}

const useUserStore = create<UserStore>((set) => ({
    user: null,
    setUser: (user) => set({user}),
    clearUser: () => set({user: null}),
}))

export default useUserStore
