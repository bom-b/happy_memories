import type {User} from '../store/userStore'
import apiClient from './client'

/**
 * 현재 인증된 사용자의 공개 프로필 정보를 조회합니다.
 * @returns 이름과 이메일
 */
export const fetchMe = () =>
    apiClient.get<User>('/users/me')
