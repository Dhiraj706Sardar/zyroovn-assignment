import React, { useState, useEffect } from 'react'
import { recordsAPI } from '../services/api'
import { useAuth } from '../context/AuthContext'
import { Plus, Edit2, Trash2, Filter, X } from 'lucide-react'
import './Transactions.css'

const Transactions = () => {
  const { user } = useAuth()
  const [records, setRecords] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [editingRecord, setEditingRecord] = useState(null)
  const [filters, setFilters] = useState({})
  const [formData, setFormData] = useState({
    amount: '',
    type: 'EXPENSE',
    category: '',
    transactionDate: new Date().toISOString().split('T')[0],
    description: '',
    notes: ''
  })

  const canModify = user?.role === 'VIEWER' || user?.role === 'ADMIN'

  useEffect(() => {
    fetchRecords()
  }, [filters])

  const fetchRecords = async () => {
    try {
      setLoading(true)
      const response = await recordsAPI.getAll(filters)
      setRecords(response.data.content || [])
    } catch (error) {
      console.error('Error fetching records:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      if (editingRecord) {
        await recordsAPI.update(editingRecord.id, formData)
      } else {
        await recordsAPI.create(formData)
      }
      setShowModal(false)
      setEditingRecord(null)
      resetForm()
      fetchRecords()
    } catch (error) {
      alert(error.response?.data?.message || 'Operation failed')
    }
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this record?')) return
    
    try {
      await recordsAPI.delete(id)
      fetchRecords()
    } catch (error) {
      alert(error.response?.data?.message || 'Delete failed')
    }
  }

  const handleEdit = (record) => {
    setEditingRecord(record)
    setFormData({
      amount: record.amount,
      type: record.type,
      category: record.category,
      transactionDate: record.transactionDate,
      description: record.description || '',
      notes: record.notes || ''
    })
    setShowModal(true)
  }

  const resetForm = () => {
    setFormData({
      amount: '',
      type: 'EXPENSE',
      category: '',
      transactionDate: new Date().toISOString().split('T')[0],
      description: '',
      notes: ''
    })
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

  return (
    <div className="transactions">
      <div className="transactions-header">
        <div>
          <h1>Transactions</h1>
          <p>Manage your financial records</p>
        </div>
        {canModify && (
          <button className="btn-primary" onClick={() => { resetForm(); setShowModal(true) }}>
            <Plus size={20} />
            Add Transaction
          </button>
        )}
      </div>

      <div className="transactions-filters">
        <select
          value={filters.type || ''}
          onChange={(e) => setFilters({ ...filters, type: e.target.value || undefined })}
        >
          <option value="">All Types</option>
          <option value="INCOME">Income</option>
          <option value="EXPENSE">Expense</option>
        </select>

        <input
          type="text"
          placeholder="Category"
          value={filters.category || ''}
          onChange={(e) => setFilters({ ...filters, category: e.target.value || undefined })}
        />

        <input
          type="date"
          placeholder="Start Date"
          value={filters.startDate || ''}
          onChange={(e) => setFilters({ ...filters, startDate: e.target.value || undefined })}
        />

        <input
          type="date"
          placeholder="End Date"
          value={filters.endDate || ''}
          onChange={(e) => setFilters({ ...filters, endDate: e.target.value || undefined })}
        />

        {Object.keys(filters).length > 0 && (
          <button className="btn-clear" onClick={() => setFilters({})}>
            <X size={16} />
            Clear
          </button>
        )}
      </div>

      {loading ? (
        <div className="loading">Loading transactions...</div>
      ) : records.length === 0 ? (
        <div className="empty-state">
          <p>No transactions found</p>
          {canModify && <p>Click "Add Transaction" to get started</p>}
        </div>
      ) : (
        <div className="transactions-table">
          <table>
            <thead>
              <tr>
                <th>Date</th>
                <th>Category</th>
                <th>Description</th>
                <th>Type</th>
                <th>Amount</th>
                {user?.role !== 'VIEWER' && <th>User</th>}
                {canModify && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {records.map((record) => (
                <tr key={record.id}>
                  <td>{formatDate(record.transactionDate)}</td>
                  <td><span className="category-badge">{record.category}</span></td>
                  <td>{record.description || '-'}</td>
                  <td>
                    <span className={`type-badge ${record.type.toLowerCase()}`}>
                      {record.type}
                    </span>
                  </td>
                  <td className={`amount ${record.type.toLowerCase()}`}>
                    {formatCurrency(record.amount)}
                  </td>
                  {user?.role !== 'VIEWER' && <td>{record.username}</td>}
                  {canModify && (
                    <td>
                      <div className="actions">
                        <button className="btn-icon" onClick={() => handleEdit(record)}>
                          <Edit2 size={16} />
                        </button>
                        <button className="btn-icon danger" onClick={() => handleDelete(record.id)}>
                          <Trash2 size={16} />
                        </button>
                      </div>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showModal && (
        <div className="modal-overlay" onClick={() => { setShowModal(false); setEditingRecord(null) }}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{editingRecord ? 'Edit Transaction' : 'Add Transaction'}</h2>
              <button className="btn-close" onClick={() => { setShowModal(false); setEditingRecord(null) }}>
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleSubmit}>
              <div className="form-row">
                <div className="form-group">
                  <label>Amount *</label>
                  <input
                    type="number"
                    step="0.01"
                    value={formData.amount}
                    onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Type *</label>
                  <select
                    value={formData.type}
                    onChange={(e) => setFormData({ ...formData, type: e.target.value })}
                    required
                  >
                    <option value="INCOME">Income</option>
                    <option value="EXPENSE">Expense</option>
                  </select>
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Category *</label>
                  <input
                    type="text"
                    value={formData.category}
                    onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                    placeholder="e.g., Salary, Rent, Groceries"
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Date *</label>
                  <input
                    type="date"
                    value={formData.transactionDate}
                    onChange={(e) => setFormData({ ...formData, transactionDate: e.target.value })}
                    required
                  />
                </div>
              </div>

              <div className="form-group">
                <label>Description</label>
                <input
                  type="text"
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  placeholder="Brief description"
                />
              </div>

              <div className="form-group">
                <label>Notes</label>
                <textarea
                  value={formData.notes}
                  onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                  placeholder="Additional notes"
                  rows="3"
                />
              </div>

              <div className="modal-actions">
                <button type="button" className="btn-secondary" onClick={() => { setShowModal(false); setEditingRecord(null) }}>
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  {editingRecord ? 'Update' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

export default Transactions
