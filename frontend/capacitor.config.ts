import type {CapacitorConfig} from '@capacitor/cli';
import dotenv from 'dotenv';
import path from 'path';

dotenv.config({path: path.resolve(process.cwd(), '../.env')});

const env = process.env.CAP_ENV || 'dev';

const serverUrls: Record<string, string> = {
    dev: 'http://127.0.0.1:5173',
    prod: process.env.SERVICE_URL!
};

/**
 * 실행 명령어
 * - 개발 환경 : npx cap sync 또는 npm run sync:dev
 * - 배포용 앱 빌드 : npm run sync:prod
 */
const config: CapacitorConfig = {
    appId: 'com.potatonetwork.happymemories',
    appName: 'Happy Memories',
    webDir: 'mobile-public',
    server: {
        url: serverUrls[env],
        cleartext: env === 'dev',
        allowNavigation: [
            serverUrls[env].replace(/^https?:\/\//, '')
        ]
    },
    plugins: {
        StatusBar: {
            style: 'LIGHT',
        }
    },
};

export default config;
