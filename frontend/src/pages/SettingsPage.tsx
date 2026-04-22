import {useNavigate} from 'react-router-dom'
import {useMutation} from '@tanstack/react-query'
import ArrowBackIcon from '../assets/icon/arrow-back.svg?react'
import {logout} from '../api/auth'
import useUserStore from '../store/userStore'
import styles from './SettingsPage.module.scss'

/**
 * 설정 화면 컴포넌트 — 사용자 프로필 표시 및 로그아웃 기능 제공
 */
function SettingsPage() {
    const navigate = useNavigate()
    const user = useUserStore((s) => s.user)
    const clearUser = useUserStore((s) => s.clearUser)

    const {mutate: doLogout, isPending} = useMutation({
        mutationFn: logout,
        onSettled: () => {
            clearUser()
            navigate('/', {replace: true})
        },
    })

    /**
     * 로그아웃 버튼 클릭 핸들러
     */
    const handleLogout = () => doLogout()

    return (
        <div className={styles.page}>
            <header className={styles.header}>
                <button className={styles.backBtn} onClick={() => navigate(-1)} aria-label="뒤로가기">
                    <ArrowBackIcon width={22} height={22}/>
                </button>
                <h1 className={styles.headerTitle}>설정</h1>
            </header>

            <main className={styles.content}>
                <section className={styles.profile}>
                    <p className={styles.name}>{user?.name ?? '—'}</p>
                    <p className={styles.email}>{user?.email ?? '—'}</p>
                </section>

                <div className={styles.actions}>
                    <button className={styles.logoutBtn} onClick={handleLogout} disabled={isPending}>
                        로그아웃
                    </button>
                </div>
            </main>
        </div>
    )
}

export default SettingsPage
