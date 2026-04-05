import React, { useState, useEffect } from 'react'
import { dashboardAPI } from '../services/api'
import { useAuth } from '../context/AuthContext'
import { TrendingUp, TrendingDown, DollarSign, Activity } from 'lucide-react'
import './Dashboard.css'

const Dashboard = () => {
  const { user } = useAuth()
  const [summary, setSummary] = useState(null)
  const [recentActivity, setRecentActivity] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchDashboardData()
  }, [])

  const fetchDashboardData = async () => {
    try {
      setLoading(true)
      const [summaryRes, activityRes] = await Promise.all([
        dashboardAPI.getSummary(),
        dashboardAPI.getRecentActivity({ limit: 5 })
      ])
      
      setSummary(summaryRes.data)
      setRecentActivity(activityRes.data)
    } catch (error) {
      console.error('Error fetching dashboard data:', error)
    } finally {
      setLoading(false)
    }
  }

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0
    }).format(amount)
  }

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-IN', {
      day: 'numeric',
      month: 'short',
      year: 'numeric'
    })
  }

  if (loading) {
    return <div className="loading">Loading dashboard...</div>
  }

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <div>
          <h1>Dashboard</h1>
          <p>Welcome back, {user?.fullName || user?.username}!</p>
        </div>
        {user?.role === 'VIEWER' && (
          <div className="info-badge">
            📊 Viewing your personal finances
          </div>
        )}
        {user?.role === 'ANALYST' && (
          <div className="info-badge analyst">
            📈 Viewing company-wide data
          </div>
        )}
        {user?.role === 'ADMIN' && (
          <div className="info-badge admin">
            ⚙️ Admin Dashboard
          </div>
        )}
      </div>

      <div className="stats-grid">
        <div className="stat-card income">
          <div className="stat-icon">
            <TrendingUp size={24} />
          </div>
          <div className="stat-content">
            <div className="stat-label">Total Income</div>
            <div className="stat-value">{formatCurrency(summary?.totalIncome || 0)}</div>
          </div>
        </div>

        <div className="stat-card expense">
          <div className="stat-icon">
            <TrendingDown size={24} />
          </div>
          <div className="stat-content">
            <div className="stat-label">Total Expense</div>
            <div className="stat-value">{formatCurrency(summary?.totalExpense || 0)}</div>
          </div>
        </div>

        <div className="stat-card balance">
          <div className="stat-icon">
            <DollarSign size={24} />
          </div>
          <div className="stat-content">
            <div className="stat-label">Net Balance</div>
            <div className="stat-value">{formatCurrency(summary?.netBalance || 0)}</div>
          </div>
        </div>

        <div className="stat-card activity">
          <div className="stat-icon">
            <Activity size={24} />
          </div>
          <div className="stat-content">
            <div className="stat-label">Transactions</div>
            <div className="stat-value">{recentActivity.length}</div>
            <div className="stat-sublabel">Recent</div>
          </div>
        </div>
      </div>

      <div className="dashboard-grid">
        <div className="dashboard-card">
          <h3>Category Breakdown</h3>
          <div className="category-list">
            {summary?.categoryTotals && Object.entries(summary.categoryTotals).length > 0 ? (
              Object.entries(summary.categoryTotals)
                .sort(([, a], [, b]) => b - a)
                .slice(0, 8)
                .map(([category, amount]) => (
                  <div key={category} className="category-item">
                    <div className="category-info">
                      <span className="category-name">{category}</span>
                    </div>
                    <span className="category-amount">{formatCurrency(amount)}</span>
                  </div>
                ))
            ) : (
              <div className="empty-state">No transactions yet</div>
            )}
          </div>
        </div>

        <div className="dashboard-card">
          <h3>Recent Activity</h3>
          <div className="activity-list">
            {recentActivity.length > 0 ? (
              recentActivity.map((record) => (
                <div key={record.id} className="activity-item">
                  <div className={`activity-type ${record.type.toLowerCase()}`}>
                    {record.type === 'INCOME' ? <TrendingUp size={16} /> : <TrendingDown size={16} />}
                  </div>
                  <div className="activity-details">
                    <div className="activity-category">{record.category}</div>
                    <div className="activity-date">{formatDate(record.transactionDate)}</div>
                  </div>
                  <div className={`activity-amount ${record.type.toLowerCase()}`}>
                    {record.type === 'INCOME' ? '+' : '-'}{formatCurrency(record.amount)}
                  </div>
                </div>
              ))
            ) : (
              <div className="empty-state">No recent activity</div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default Dashboard
