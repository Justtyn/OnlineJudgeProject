import { fileURLToPath, URL } from 'node:url'
import path from 'path'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, process.cwd(), '')
    const baseApiUrl = (env.VITE_BASE_URL || 'http://localhost:9090').replace(/\/$/, '')

    return {
        plugins: [
            vue(),
            vueDevTools(),
        ],
        resolve: {
            alias: {
                '@': path.resolve(__dirname, 'src')
            },
        },
        server: {
            proxy: {
                '/problem': {
                    target: baseApiUrl,
                    changeOrigin: true
                },
                // '/api': {
                //   target: baseApiUrl,
                //   changeOrigin: true,
                //   rewrite: (path) => path.replace(/^\/api/, '')
                // }
                '/api': {
                    target: baseApiUrl,
                    changeOrigin: true,
                    // rewrite: (path) => path.replace(/^\/api/, '')
                }
            }
        },
        preview: {
            allowedHosts: [
                'c6d5f828.natappfree.cc'  // ← 把你的 natapp 域名放这里
            ],
            port: 4173 // 可保持默认或改为你的预览端口
        }
    }
})
