import axios from 'axios'
import useUserStore from '../store/userStore'

/**
 * 백엔드 API 통신용 axios 인스턴스.
 * - baseURL: `/api` — Vite 개발 프록시가 localhost:8080/api로 전달하고,
 *   운영 환경에서는 동일 컴포즈 내 nginx가 /api를 백엔드 컨테이너로 라우팅한다.
 * - withCredentials: HttpOnly Cookie JWT 자동 전송을 위해 활성화.
 */
const apiClient = axios.create({
    baseURL: '/api',
    withCredentials: true,
})

/**
 * 401 응답 시 사용자 상태를 초기화하고 로그인 페이지로 이동합니다.
 * 그 외 4xx/5xx 응답에서 서버가 message를 내려줄 경우 전역 알림창을 표시합니다.
 * 로그인 엔드포인트 자체의 401은 제외합니다.
 */
apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        const isLoginEndpoint = error.config?.url?.includes('/auth/login')
        const status: number | undefined = error.response?.status

        if (status === 401 && !isLoginEndpoint) {
            useUserStore.getState().clearUser()
            if (window.location.pathname !== '/') {
                alert('인증이 만료되었습니다. 다시 로그인해 주세요.')
                window.location.replace('/')
            }
        } else if (status !== undefined && status !== 401) {
            const message: string | undefined = error.response?.data?.message
            if (message) alert(message)
        }

        return Promise.reject(error)
    },
)

export default apiClient
