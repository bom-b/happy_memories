import {useEffect} from 'react'
import {Routes, Route, Navigate} from 'react-router-dom'
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

    /**
     * 앱 마운트 시 서버에서 사용자 정보를 조회하여 전역 스토어에 복원합니다.
     * 쿠키가 만료되었으면 401 인터셉터가 로그인 페이지로 이동시킵니다.
     */
    useEffect(() => {
        fetchMe()
            .then(({data}) => setUser(data))
            .catch(() => {})
    }, [])

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

