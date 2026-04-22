import {useState} from 'react'
import type {FormEvent} from 'react'
import {useNavigate} from 'react-router-dom'
import {useMutation} from '@tanstack/react-query'
import {login} from '../api/auth'
import {fetchMe} from '../api/user'
import useUserStore from '../store/userStore'
import styles from './LoginPage.module.scss'

/**
 * 로그인 화면 컴포넌트
 */
function LoginPage() {
    const navigate = useNavigate()
    const setUser = useUserStore((s) => s.setUser)
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')

    const {mutate, isPending, isError} = useMutation({
        mutationFn: login,
        onSuccess: async () => {
            const {data} = await fetchMe()
            setUser(data)
            navigate('/feed')
        },
    })

    /**
     * 폼 제출 핸들러 — 로그인 API를 호출하고 성공 시 피드로 이동
     */
    const handleSubmit = (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault()
        mutate({email, password})
    }

    return (
        <div className={styles.page}>
            <div className={styles.container}>
                <div className={styles.header}>
                    <h1 className={styles.title}>
                        Happy<br/>Memories
                    </h1>
                    <p className={styles.subtitle}>
                        웹·앱 크로스 플랫폼<br/>일기 서비스
                    </p>
                </div>

                <form className={styles.form} onSubmit={handleSubmit}>
                    <input
                        type="text"
                        placeholder="Email"
                        className={styles.input}
                        autoComplete="username"
                        value={email}
                        onChange={e => setEmail(e.target.value)}
                    />
                    <input
                        type="password"
                        placeholder="Password"
                        className={styles.input}
                        autoComplete="current-password"
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                    />
                    {isError && <p className={styles.error}>이메일 또는 비밀번호가 올바르지 않습니다.</p>}
                    <button type="submit" className={styles.loginButton} disabled={isPending}>
                        {isPending ? '로그인 중...' : '로그인'}
                    </button>
                </form>
            </div>
        </div>
    )
}

export default LoginPage
