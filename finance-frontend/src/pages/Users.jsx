import React, { useState, useEffect } from 'react'
import { usersAPI } from '../services/api'
import { UserPlus, Shield, Power, PowerOff } from 'lucide-react'
import './Users.css'

const Users = () => {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchUsers()
  }, [])

  const fetchUsers = async () => {
    try {
      setLoading(true)
      const response = await usersAPI.getAll()
      setUsers(response.data)
    } catch (error) {
      console.error('Error fetching users:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleActivate = async (id) => {
    if (!window.confirm('Are you sure you want to activate this user?')) return
    
    try {
      await usersAPI.activate(id)
      fetchUsers()
    } catch (error) {
      alert(error.response?.data?.message || 'Activation failed')
    }
  }

  const handleDeactivate = async (id) => {
    if (!window.confirm('Are you sure you want to deactivate this user?')) return
    
    try {
      await usersAPI.deactivate(id)
      fetchUsers()
    } catch (error) {
      alert(error.response?.data?.message || 'Deactivation failed')
    }
  }

  const getRoleBadgeColor = (role) => {
    switch (role) {
      case 'ADMIN': return '#ef4444'
      case 'ANALYST': return '#3b82f6'
      case 'VIEWER': return '#10b981'
      default: return '#6b7280'
    }
  }

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-IN', {
      day: 'numeric',
      month: 'short',
      year: 'numeric'
    })
  }

  if (loading) {
    return <div className="loading">Loading users...</div>
  }

  return (
    <div className="users">
      <div className="users-header">
        <div>
          <h1>User Management</h1>
          <p>Manage system users and roles</p>
        </div>
        <div className="info-badge">
          <Shield size={16} />
          Admin Only
        </div>
      </div>

      <div className="users-grid">
        {users.map((user) => (
          <div key={user.id} className="user-card">
            <div className="user-avatar-large">
              {user.fullName?.charAt(0) || user.username?.charAt(0) || 'U'}
            </div>
            <div className="user-info-section">
              <h3>{user.fullName || user.username}</h3>
              <p className="user-email">{user.email}</p>
              <p className="user-username">@{user.username}</p>
            </div>
            <div className="user-meta">
              <div 
                className="role-badge-large" 
                style={{ backgroundColor: getRoleBadgeColor(user.role) }}
              >
                {user.role}
              </div>
              <div className={`status-badge ${user.status.toLowerCase()}`}>
                {user.status}
              </div>
            </div>
            <div className="user-dates">
              <div className="date-item">
                <span className="date-label">Joined:</span>
                <span className="date-value">{formatDate(user.createdAt)}</span>
              </div>
            </div>
            <div className="user-actions">
              {user.status === 'ACTIVE' ? (
                <button 
                  className="btn-deactivate" 
                  onClick={() => handleDeactivate(user.id)}
                  title="Deactivate user"
                >
                  <PowerOff size={16} />
                  Deactivate
                </button>
              ) : (
                <button 
                  className="btn-activate" 
                  onClick={() => handleActivate(user.id)}
                  title="Activate user"
                >
                  <Power size={16} />
                  Activate
                </button>
              )}
            </div>
          </div>
        ))}
      </div>

      {users.length === 0 && (
        <div className="empty-state">No users found</div>
      )}
    </div>
  )
}

export default Users
