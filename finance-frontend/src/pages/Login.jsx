import React, { useState, useEffect } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import './Auth.css'

const Login = () => {
    const location = useLocation()
    const [formData, setFormData] = useState({ username: '', password: '' })
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)
    const [showModal, setShowModal] = useState(false)
    const [modalMessage, setModalMessage] = useState('')
    const { login } = useAuth()
    const navigate = useNavigate()

    // Get success message from registration
    const successMessage = location.state?.message

    // Debug: Log when error changes
    useEffect(() => {
        if (error) {
            console.log('Error set:', error)
        }
    }, [error])

    const handleSubmit = async (e) => {
        e.preventDefault()
        console.log('Form submitted')
        setLoading(true)

        const result = await login(formData)
        console.log('Login result:', result)

        setLoading(false)

        if (result.success) {
            setError('')
            navigate('/dashboard')
        } else {
            console.log('Setting error:', result.error)
            const errorMessage = result.error || 'Login failed'
            setError(errorMessage)
            
            // Show modal for inactive account
            if (errorMessage.toLowerCase().includes('inactive')) {
                setModalMessage(errorMessage)
                setShowModal(true)
            }
        }
    }

    // Check if error is about inactive account
    const isInactiveAccount = error && error.toLowerCase().includes('inactive')

    return (
        <div className="auth-container">
            <div className="auth-card">
                <div className="auth-header">
                    <h1>💰 FinTrack</h1>
                    <h2>Welcome Back</h2>
                    <p>Login to manage your finances</p>
                </div>

                <form onSubmit={handleSubmit} className="auth-form">
                    {successMessage && <div className="success-message">{successMessage}</div>}
                    
                    {error && !isInactiveAccount && (
                        <div className="error-message">
                            <strong>❌ Login Failed</strong>
                            <div style={{ marginTop: '4px' }}>{error}</div>
                        </div>
                    )}

                    <div className="form-group">
                        <label>Username</label>
                        <input
                            type="text"
                            value={formData.username}
                            onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                            placeholder="Enter your username"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label>Password</label>
                        <input
                            type="password"
                            value={formData.password}
                            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                            placeholder="Enter your password"
                            required
                        />
                    </div>

                    <button type="submit" className="auth-btn" disabled={loading}>
                        {loading ? 'Logging in...' : 'Login'}
                    </button>
                </form>

                <div className="auth-footer">
                    <p>Don't have an account? <Link to="/register">Register</Link></p>
                </div>

                <div className="demo-accounts">
                    <p className="demo-title">Demo Accounts:</p>
                    <div className="demo-list">
                        <div className="demo-item">
                            <strong>Viewer:</strong> viewer / viewer
                        </div>
                        <div className="demo-item">
                            <strong>Analyst:</strong> analyst / analyst
                        </div>
                        <div className="demo-item">
                            <strong>Admin:</strong> admin / admin
                        </div>
                    </div>
                </div>
            </div>

            {/* Deactivated Account Modal */}
            {showModal && (
                <div className="modal-overlay-alert" onClick={() => setShowModal(false)}>
                    <div className="modal-alert" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-alert-icon">⚠️</div>
                        <h2 className="modal-alert-title">Account Deactivated</h2>
                        <p className="modal-alert-message">{modalMessage}</p>
                        <div className="modal-alert-help">
                            💡 Please contact your system administrator to reactivate your account.
                        </div>
                        <button 
                            className="modal-alert-button" 
                            onClick={() => setShowModal(false)}
                        >
                            I Understand
                        </button>
                    </div>
                </div>
            )}
        </div>
    )
}

export default Login
