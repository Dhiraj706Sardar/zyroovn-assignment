import React, { useState, useEffect } from 'react'
import { dashboardAPI } from '../services/api'
import { TrendingUp, TrendingDown, DollarSign, BarChart3 } from 'lucide-react'
import {
  LineChart, Line, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer,
  Area, AreaChart
} from 'recharts'
import './Analytics.css'

const Analytics = () => {
  const [monthlyTrends, setMonthlyTrends] = useState({})
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchAnalytics()
  }, [])

  const fetchAnalytics = async () => {
    try {
      setLoading(true)
      const response = await dashboardAPI.getMonthlyTrends({ months: 6 })
      setMonthlyTrends(response.data)
    } catch (error) {
      console.error('Error fetching analytics:', error)
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

  // Prepare data for charts
  const prepareChartData = () => {
    return Object.entries(monthlyTrends)
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([month, data]) => ({
        month: month,
        income: data.income || 0,
        expense: data.expense || 0,
        balance: data.balance || 0
      }))
  }

  // Prepare data for pie chart (total income vs expense)
  const preparePieData = () => {
    const chartData = prepareChartData()
    const totalIncome = chartData.reduce((sum, item) => sum + item.income, 0)
    const totalExpense = chartData.reduce((sum, item) => sum + item.expense, 0)

    return [
      { name: 'Income', value: totalIncome, color: '#10b981' },
      { name: 'Expense', value: totalExpense, color: '#ef4444' }
    ]
  }

  // Calculate summary stats
  const calculateStats = () => {
    const chartData = prepareChartData()
    const totalIncome = chartData.reduce((sum, item) => sum + item.income, 0)
    const totalExpense = chartData.reduce((sum, item) => sum + item.expense, 0)
    const avgIncome = chartData.length > 0 ? totalIncome / chartData.length : 0
    const avgExpense = chartData.length > 0 ? totalExpense / chartData.length : 0

    return {
      totalIncome,
      totalExpense,
      netBalance: totalIncome - totalExpense,
      avgIncome,
      avgExpense
    }
  }

  const chartData = prepareChartData()
  const pieData = preparePieData()
  const stats = calculateStats()

  const COLORS = ['#10b981', '#ef4444']

  if (loading) {
    return <div className="loading">Loading analytics...</div>
  }

  if (chartData.length === 0) {
    return (
      <div className="analytics">
        <div className="analytics-header">
          <div>
            <h1>Analytics & Insights</h1>
            <p>Monthly trends and financial insights</p>
          </div>
        </div>
        <div className="empty-state">No data available for analysis</div>
      </div>
    )
  }

  return (
    <div className="analytics">
      <div className="analytics-header">
        <div>
          <h1>Analytics & Insights</h1>
          <p>Monthly trends and financial insights</p>
        </div>
        <div className="info-badge">
          <TrendingUp size={16} />
          Company-wide data
        </div>
      </div>

      {/* Summary Stats */}
      <div className="analytics-stats">
        <div className="stat-card-analytics income">
          <div className="stat-icon-analytics">
            <TrendingUp size={24} />
          </div>
          <div className="stat-content-analytics">
            <div className="stat-label-analytics">Total Income</div>
            <div className="stat-value-analytics">{formatCurrency(stats.totalIncome)}</div>
            <div className="stat-sublabel-analytics">Avg: {formatCurrency(stats.avgIncome)}/month</div>
          </div>
        </div>

        <div className="stat-card-analytics expense">
          <div className="stat-icon-analytics">
            <TrendingDown size={24} />
          </div>
          <div className="stat-content-analytics">
            <div className="stat-label-analytics">Total Expense</div>
            <div className="stat-value-analytics">{formatCurrency(stats.totalExpense)}</div>
            <div className="stat-sublabel-analytics">Avg: {formatCurrency(stats.avgExpense)}/month</div>
          </div>
        </div>

        <div className="stat-card-analytics balance">
          <div className="stat-icon-analytics">
            <DollarSign size={24} />
          </div>
          <div className="stat-content-analytics">
            <div className="stat-label-analytics">Net Balance</div>
            <div className="stat-value-analytics">{formatCurrency(stats.netBalance)}</div>
            <div className="stat-sublabel-analytics">Last {chartData.length} months</div>
          </div>
        </div>

        <div className="stat-card-analytics trend">
          <div className="stat-icon-analytics">
            <BarChart3 size={24} />
          </div>
          <div className="stat-content-analytics">
            <div className="stat-label-analytics">Months Analyzed</div>
            <div className="stat-value-analytics">{chartData.length}</div>
            <div className="stat-sublabel-analytics">Data points</div>
          </div>
        </div>
      </div>

      {/* Charts Grid */}
      <div className="charts-grid">
        {/* Line Chart - Trend Over Time */}
        <div className="chart-card full-width">
          <h3>Income vs Expense Trend</h3>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
              <XAxis
                dataKey="month"
                stroke="#6b7280"
                style={{ fontSize: '12px' }}
              />
              <YAxis
                stroke="#6b7280"
                style={{ fontSize: '12px' }}
                tickFormatter={(value) => `₹${(value / 1000).toFixed(0)}k`}
              />
              <Tooltip
                formatter={(value) => formatCurrency(value)}
                contentStyle={{
                  backgroundColor: 'white',
                  border: '1px solid #e5e7eb',
                  borderRadius: '8px'
                }}
              />
              <Legend />
              <Line
                type="monotone"
                dataKey="income"
                stroke="#10b981"
                strokeWidth={3}
                dot={{ fill: '#10b981', r: 5 }}
                activeDot={{ r: 7 }}
                name="Income"
              />
              <Line
                type="monotone"
                dataKey="expense"
                stroke="#ef4444"
                strokeWidth={3}
                dot={{ fill: '#ef4444', r: 5 }}
                activeDot={{ r: 7 }}
                name="Expense"
              />
            </LineChart>
          </ResponsiveContainer>
        </div>

        {/* Area Chart - Balance Over Time */}
        <div className="chart-card full-width">
          <h3>Net Balance Trend</h3>
          <ResponsiveContainer width="100%" height={300}>
            <AreaChart data={chartData}>
              <defs>
                <linearGradient id="colorBalance" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.8} />
                  <stop offset="95%" stopColor="#3b82f6" stopOpacity={0.1} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
              <XAxis
                dataKey="month"
                stroke="#6b7280"
                style={{ fontSize: '12px' }}
              />
              <YAxis
                stroke="#6b7280"
                style={{ fontSize: '12px' }}
                tickFormatter={(value) => `₹${(value / 1000).toFixed(0)}k`}
              />
              <Tooltip
                formatter={(value) => formatCurrency(value)}
                contentStyle={{
                  backgroundColor: 'white',
                  border: '1px solid #e5e7eb',
                  borderRadius: '8px'
                }}
              />
              <Area
                type="monotone"
                dataKey="balance"
                stroke="#3b82f6"
                strokeWidth={2}
                fillOpacity={1}
                fill="url(#colorBalance)"
                name="Balance"
              />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        {/* Bar Chart - Monthly Comparison */}
        <div className="chart-card">
          <h3>Monthly Comparison</h3>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
              <XAxis
                dataKey="month"
                stroke="#6b7280"
                style={{ fontSize: '12px' }}
              />
              <YAxis
                stroke="#6b7280"
                style={{ fontSize: '12px' }}
                tickFormatter={(value) => `₹${(value / 1000).toFixed(0)}k`}
              />
              <Tooltip
                formatter={(value) => formatCurrency(value)}
                contentStyle={{
                  backgroundColor: 'white',
                  border: '1px solid #e5e7eb',
                  borderRadius: '8px'
                }}
              />
              <Legend />
              <Bar dataKey="income" fill="#10b981" name="Income" radius={[8, 8, 0, 0]} />
              <Bar dataKey="expense" fill="#ef4444" name="Expense" radius={[8, 8, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Pie Chart - Income vs Expense Distribution */}
        <div className="chart-card">
          <h3>Income vs Expense Distribution</h3>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={pieData}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                outerRadius={100}
                fill="#8884d8"
                dataKey="value"
              >
                {pieData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip
                formatter={(value) => formatCurrency(value)}
                contentStyle={{
                  backgroundColor: 'white',
                  border: '1px solid #e5e7eb',
                  borderRadius: '8px'
                }}
              />
            </PieChart>
          </ResponsiveContainer>
          <div className="pie-legend">
            <div className="pie-legend-item">
              <div className="pie-legend-color" style={{ backgroundColor: '#10b981' }}></div>
              <span>Income: {formatCurrency(pieData[0].value)}</span>
            </div>
            <div className="pie-legend-item">
              <div className="pie-legend-color" style={{ backgroundColor: '#ef4444' }}></div>
              <span>Expense: {formatCurrency(pieData[1].value)}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Monthly Details Table */}
      <div className="chart-card full-width">
        <h3>Monthly Breakdown</h3>
        <div className="monthly-table">
          <table>
            <thead>
              <tr>
                <th>Month</th>
                <th>Income</th>
                <th>Expense</th>
                <th>Balance</th>
                <th>Savings Rate</th>
              </tr>
            </thead>
            <tbody>
              {chartData.map((row) => {
                const savingsRate = row.income > 0
                  ? ((row.balance / row.income) * 100).toFixed(1)
                  : 0
                return (
                  <tr key={row.month}>
                    <td className="month-cell">{row.month}</td>
                    <td className="income-cell">{formatCurrency(row.income)}</td>
                    <td className="expense-cell">{formatCurrency(row.expense)}</td>
                    <td className={`balance-cell ${row.balance >= 0 ? 'positive' : 'negative'}`}>
                      {formatCurrency(row.balance)}
                    </td>
                    <td className="savings-cell">
                      <div className="savings-bar">
                        <div
                          className="savings-fill"
                          style={{
                            width: `${Math.min(Math.abs(savingsRate), 100)}%`,
                            backgroundColor: savingsRate >= 0 ? '#10b981' : '#ef4444'
                          }}
                        ></div>
                        <span className="savings-text">{savingsRate}%</span>
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}

export default Analytics
