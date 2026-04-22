import apiClient from './client'

export interface LoginRequest {
    email: string
    password: string
}

/**
 * 로그인 API 호출. 성공 시 서버가 HttpOnly Cookie에 JWT를 설정합니다.
 * @param data 이메일과 비밀번호
 */
export const login = (data: LoginRequest) =>
    apiClient.post('/auth/login', data)

/**
 * 로그아웃 API 호출. 서버가 JWT 쿠키를 만료 처리합니다.
 */
export const logout = () =>
    apiClient.post('/auth/logout')
