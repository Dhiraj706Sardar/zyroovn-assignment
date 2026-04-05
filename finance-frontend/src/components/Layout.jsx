import React from 'react'
import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { LayoutDashboard, Receipt, TrendingUp, Users, LogOut } from 'lucide-react'
import './Layout.css'

const Layout = () => {
  const { user, logout } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const navItems = [
    { path: '/dashboard', label: 'Dashboard', icon: LayoutDashboard, roles: ['VIEWER', 'ANALYST', 'ADMIN'] },
    { path: '/transactions', label: 'Transactions', icon: Receipt, roles: ['VIEWER', 'ANALYST', 'ADMIN'] },
    { path: '/analytics', label: 'Analytics', icon: TrendingUp, roles: ['ANALYST', 'ADMIN'] },
    { path: '/users', label: 'Users', icon: Users, roles: ['ADMIN'] }
  ]

  const filteredNavItems = navItems.filter(item => item.roles.includes(user?.role))

  const getRoleBadgeColor = (role) => {
    switch (role) {
      case 'ADMIN': return '#ef4444'
      case 'ANALYST': return '#3b82f6'
      case 'VIEWER': return '#10b981'
      default: return '#6b7280'
    }
  }

  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="sidebar-header">
          <h1>💰 FinTrack</h1>
          <p className="tagline">Personal Finance Manager</p>
        </div>

        <nav className="nav">
          {filteredNavItems.map(item => {
            const Icon = item.icon
            const isActive = location.pathname === item.path
            return (
              <Link
                key={item.path}
                to={item.path}
                className={`nav-item ${isActive ? 'active' : ''}`}
              >
                <Icon size={20} />
                <span>{item.label}</span>
              </Link>
            )
          })}
        </nav>

        <div className="sidebar-footer">
          <div className="user-info">
            <div className="user-avatar">
              {user?.fullName?.charAt(0) || user?.username?.charAt(0) || 'U'}
            </div>
            <div className="user-details">
              <div className="user-name">{user?.fullName || user?.username}</div>
              <div 
                className="user-role" 
                style={{ backgroundColor: getRoleBadgeColor(user?.role) }}
              >
                {user?.role}
              </div>
            </div>
          </div>
          <button onClick={handleLogout} className="logout-btn">
            <LogOut size={18} />
            <span>Logout</span>
          </button>
        </div>
      </aside>

      <main className="main-content">
        <Outlet />
      </main>
    </div>
  )
}

export default Layout
