import {useEffect, useRef} from 'react'
import {Routes, Route, Navigate, useNavigate, useLocation} from 'react-router-dom'
import {Capacitor} from '@capacitor/core'
import {App as CapApp} from '@capacitor/app'
import {Toast} from '@capacitor/toast'
import {modalStack} from './utils/modalStack'
import styles from './App.module.scss'
import LoginPage from './pages/LoginPage'
import FeedPage from './pages/FeedPage.tsx'
import SettingsPage from './pages/SettingsPage'
import WritePage from './pages/WritePage'
import GalleryPage from './pages/GalleryPage'
import CalendarPage from './pages/CalendarPage'
import {fetchMe} from './api/user'
import useUserStore from './store/userStore'

/**
 * 최상위 앱 컴포넌트 — URL 기반 라우팅을 관리하고 앱 진입 시 인증 상태를 복원합니다.
 */
function App() {
    const setUser = useUserStore((s) => s.setUser)
    const user = useUserStore((s) => s.user)
    const navigate = useNavigate()
    const location = useLocation()
    const exitReadyRef = useRef(false)

    /**
     * 포스트스테이트 이벤트(iOS 스와이프, 브라우저 백) 발생 시 모달이 열려 있으면 최상단 모달을 닫습니다.
     */
    useEffect(() => {
        const handlePop = () => {
            if (modalStack.size() > 0) modalStack.pop()
        }
        window.addEventListener('popstate', handlePop)
        return () => window.removeEventListener('popstate', handlePop)
    }, [])

    /**
     * Capacitor 네이티브 환경에서 시스템 뒤로가기 버튼을 처리합니다.
     * 모달이 열려 있으면 모달을 닫고, 루트 경로에서는 종료 확인 토스트를 띄우고,
     * 다른 경로에서는 이전 경로로 이동합니다.
     */
    useEffect(() => {
        if (!Capacitor.isNativePlatform()) return
        const listener = CapApp.addListener('backButton', () => {
            if (modalStack.size() > 0) {
                window.history.back()
                return
            }
            const isRootPath = location.pathname === '/' || location.pathname === '/feed'
            if (isRootPath) {
                if (exitReadyRef.current) {
                    CapApp.exitApp()
                    return
                }
                exitReadyRef.current = true
                Toast.show({text: '한 번 더 누르면 종료됩니다', duration: 'short', position: 'bottom'})
                setTimeout(() => { exitReadyRef.current = false }, 2000)
            } else {
                navigate(-1)
            }
        })
        return () => { listener.then(h => h.remove()) }
    }, [location.pathname, navigate])

    /**
     * 앱 마운트 시 서버에서 사용자 정보를 조회하여 전역 스토어에 복원합니다.
     * 쿠키가 만료되었으면 401 인터셉터가 로그인 페이지로 이동시킵니다.
     */
    useEffect(() => {
        fetchMe()
            .then(({data}) => setUser(data))
            .catch(() => {})
    }, [setUser])

    return (
        <div className={styles.appWrapper}>
            <Routes>
                <Route path="/" element={user ? <Navigate to="/feed" replace/> : <LoginPage/>}/>
                <Route path="/feed" element={<FeedPage/>}/>
                <Route path="/feed/write" element={<WritePage/>}/>
                <Route path="/settings" element={<SettingsPage/>}/>
                <Route path="/gallery" element={<GalleryPage/>}/>
                <Route path="/calendar" element={<CalendarPage/>}/>
                <Route path="*" element={<Navigate to="/" replace/>}/>
            </Routes>
        </div>
    )
}

export default App

