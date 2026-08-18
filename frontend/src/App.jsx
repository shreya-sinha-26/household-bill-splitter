import { Link, Route, Routes } from 'react-router-dom'
import GroupListPage from './pages/GroupListPage.jsx'
import GroupDetailPage from './pages/GroupDetailPage.jsx'

export default function App() {
  return (
    <div className="app">
      <header className="app-header">
        <Link to="/" className="app-brand">Household Bill Splitter</Link>
      </header>
      <main className="app-main">
        <Routes>
          <Route path="/" element={<GroupListPage />} />
          <Route path="/groups/:id" element={<GroupDetailPage />} />
        </Routes>
      </main>
    </div>
  )
}
