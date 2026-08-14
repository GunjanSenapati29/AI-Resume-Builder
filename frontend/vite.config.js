import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// Proxies /api/* to the Spring Boot backend during development, so the
// browser sees everything as same-origin (localhost:5173) and there's no
// CORS to configure on the backend for now. A real deployment (separate
// frontend/backend origins) will need actual CORS config - not needed yet.
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
