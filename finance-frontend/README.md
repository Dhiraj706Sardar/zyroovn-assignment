# FinTrack Frontend

Modern React frontend for the Finance Backend API - A personal finance tracking application with role-based access control.

## Features

- 🔐 **Authentication**: Login and registration with JWT
- 📊 **Dashboard**: Real-time financial overview with stats
- 💰 **Transactions**: Create, view, edit, and delete financial records
- 📈 **Analytics**: Monthly trends and insights (Analyst/Admin only)
- 👥 **User Management**: Manage users and roles (Admin only)
- 🎨 **Modern UI**: Clean, responsive design with smooth animations
- 🔒 **Role-Based Access**: Different features for VIEWER, ANALYST, and ADMIN

## Tech Stack

- **React 18**: Modern React with hooks
- **React Router 6**: Client-side routing
- **Axios**: HTTP client for API calls
- **Lucide React**: Beautiful icons
- **Vite**: Fast build tool and dev server

## Prerequisites

- Node.js 16+ and npm/yarn
- Finance Backend API running on `http://localhost:8080`

## Quick Start

### 1. Install Dependencies

```bash
cd finance-frontend
npm install
```

### 2. Start Development Server

```bash
npm run dev
```

The app will open at `http://localhost:3000`

### 3. Login with Demo Accounts

```
Viewer (Personal Finance):
Username: viewer
Password: viewer

Analyst (Company Data):
Username: analyst
Password: analyst

Admin (Full Control):
Username: admin
Password: admin
```

## Project Structure

```
finance-frontend/
├── src/
│   ├── components/
│   │   └── Layout.jsx          # Main layout with sidebar
│   ├── context/
│   │   └── AuthContext.jsx     # Authentication context
│   ├── pages/
│   │   ├── Login.jsx           # Login page
│   │   ├── Register.jsx        # Registration page
│   │   ├── Dashboard.jsx       # Dashboard with stats
│   │   ├── Transactions.jsx    # Transaction management
│   │   ├── Analytics.jsx       # Monthly trends
│   │   └── Users.jsx           # User management
│   ├── services/
│   │   └── api.js              # API service layer
│   ├── App.jsx                 # Main app component
│   ├── main.jsx                # Entry point
│   └── index.css               # Global styles
├── index.html
├── vite.config.js
└── package.json
```

## Features by Role

### 👁️ VIEWER (Personal Finance User)

**Can Do**:
- ✅ View personal dashboard with own financial summary
- ✅ Create own income/expense records
- ✅ View own transaction history
- ✅ Edit own transactions
- ✅ Delete own transactions
- ✅ Filter own transactions by date, category, type

**Cannot Do**:
- ❌ See other users' data
- ❌ Access analytics/insights
- ❌ Manage users

**Use Case**: Rahul tracks his personal salary, rent, groceries, and other expenses.

---

### 📊 ANALYST (Finance Analyst)

**Can Do**:
- ✅ View company-wide dashboard with all users' data
- ✅ View all users' transactions
- ✅ Access monthly trends and insights
- ✅ Filter and analyze data across the system

**Cannot Do**:
- ❌ Create, edit, or delete any transactions
- ❌ Manage users

**Use Case**: Priya analyzes spending patterns across all users and generates reports.

---

### ⚙️ ADMIN (System Administrator)

**Can Do**:
- ✅ Everything ANALYST can do
- ✅ Create transactions for any user
- ✅ Edit any transaction
- ✅ Delete any transaction
- ✅ View all users
- ✅ Manage user accounts

**Use Case**: Amit manages the system, fixes incorrect data, and onboards new users.

---

## API Integration

The frontend connects to the backend API at `http://localhost:8080/api`.

### API Endpoints Used

```javascript
// Authentication
POST /api/auth/login
POST /api/auth/register

// Financial Records
GET    /api/records
GET    /api/records/{id}
POST   /api/records
PUT    /api/records/{id}
DELETE /api/records/{id}

// Dashboard
GET /api/dashboard/summary
GET /api/dashboard/category-totals
GET /api/dashboard/recent-activity
GET /api/dashboard/monthly-trends

// Users (Admin only)
GET    /api/users
GET    /api/users/{id}
POST   /api/users
PUT    /api/users/{id}
DELETE /api/users/{id}
```

## Development

### Available Scripts

```bash
# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

### Environment Variables

The app uses Vite's proxy configuration to forward API requests to the backend. No environment variables needed for development.

For production, update the `baseURL` in `src/services/api.js`:

```javascript
const api = axios.create({
  baseURL: 'https://your-backend-api.com/api'
})
```

## Building for Production

```bash
# Build optimized production bundle
npm run build

# Output will be in the 'dist' folder
# Deploy the 'dist' folder to your hosting service
```

## Deployment

### Option 1: Vercel

```bash
npm install -g vercel
vercel
```

### Option 2: Netlify

```bash
npm install -g netlify-cli
netlify deploy --prod
```

### Option 3: Static Hosting

Build the app and upload the `dist` folder to any static hosting service (AWS S3, GitHub Pages, etc.)

## Troubleshooting

### API Connection Issues

If you see "Network Error" or API calls fail:

1. Ensure backend is running on `http://localhost:8080`
2. Check CORS configuration in backend
3. Verify JWT token is valid (check browser console)

### Login Issues

If login fails with 401:

1. Verify username/password are correct
2. Check if users exist in database
3. Ensure backend authentication is working

### Build Issues

If build fails:

```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install

# Try building again
npm run build
```

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

MIT License

## Support

For issues or questions, please open an issue on GitHub or contact the development team.

---

**Happy Finance Tracking! 💰**
