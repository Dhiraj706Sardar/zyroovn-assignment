import axios from 'axios'

const api = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json'
    }
})

// Add token to requests
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => Promise.reject(error)
)

// Handle 401 errors
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token')
            localStorage.removeItem('user')
            window.location.href = '/login'
        }
        return Promise.reject(error)
    }
)

// Auth APIs
export const authAPI = {
    login: (credentials) => api.post('/auth/login', credentials),
    register: (userData) => api.post('/auth/register', userData)
}

// Financial Records APIs
export const recordsAPI = {
    getAll: (params) => api.get('/records', { params }),
    getById: (id) => api.get(`/records/${id}`),
    create: (data) => api.post('/records', data),
    update: (id, data) => api.put(`/records/${id}`, data),
    delete: (id) => api.delete(`/records/${id}`)
}

// Dashboard APIs
export const dashboardAPI = {
    getSummary: (params) => api.get('/dashboard/summary', { params }),
    getCategoryTotals: (params) => api.get('/dashboard/category-totals', { params }),
    getRecentActivity: (params) => api.get('/dashboard/recent-activity', { params }),
    getMonthlyTrends: (params) => api.get('/dashboard/monthly-trends', { params })
}

// User Management APIs
export const usersAPI = {
    getAll: () => api.get('/users'),
    getById: (id) => api.get(`/users/${id}`),
    create: (data) => api.post('/users', data),
    update: (id, data) => api.put(`/users/${id}`, data),
    delete: (id) => api.delete(`/users/${id}`),
    deactivate: (id) => api.patch(`/users/${id}/deactivate`),
    activate: (id) => api.patch(`/users/${id}/activate`)
}

export default api
