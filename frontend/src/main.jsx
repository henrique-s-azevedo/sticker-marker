/**
 * Application entry point — mounts the React tree into #root.
 * Global styles are imported here to ensure they are applied before any component renders.
 */
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './styles/global.css'
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
