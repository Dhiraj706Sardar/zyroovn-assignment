# FinTrack Frontend - Quick Setup Guide

## 🚀 Quick Start (5 minutes)

### Step 1: Install Dependencies

```bash
cd finance-frontend
npm install
```

This will install:
- React 18
- React Router 6
- Axios
- Lucide React (icons)
- Vite

### Step 2: Start the Backend

Make sure your Finance Backend is running:

```bash
# In the backend directory
cd ../
mvn spring-boot:run
```

Backend should be running on `http://localhost:8080`

### Step 3: Start the Frontend

```bash
# In the finance-frontend directory
npm run dev
```

Frontend will start on `http://localhost:3000`

### Step 4: Login

Open `http://localhost:3000` in your browser and login with:

```
Username: viewer
Password: viewer
```

Or register a new account!

---

## 📱 What You'll See

### 1. Login Page
- Clean, modern login form
- Demo account credentials displayed
- Link to registration

### 2. Dashboard (All Roles)
- Total Income, Expense, Net Balance cards
- Category breakdown
- Recent activity list
- Role-specific badge (VIEWER/ANALYST/ADMIN)

### 3. Transactions Page
- **VIEWER**: Can create, edit, delete own transactions
- **ANALYST**: Can view all transactions (read-only)
- **ADMIN**: Can manage any transaction

Features:
- Add new transaction button
- Filter by type, category, date range
- Edit/Delete buttons (if allowed)
- Beautiful table view

### 4. Analytics Page (ANALYST/ADMIN only)
- Monthly trends for last 6 months
- Income, Expense, Balance per month
- Company-wide data

### 5. Users Page (ADMIN only)
- View all system users
- See user roles and status
- User cards with avatars

---

## 🎨 UI Features

- **Responsive Design**: Works on desktop, tablet, and mobile
- **Modern Gradient**: Purple gradient theme
- **Smooth Animations**: Hover effects and transitions
- **Role-Based UI**: Different features based on user role
- **Clean Icons**: Lucide React icons throughout
- **Color-Coded**: Income (green), Expense (red), Balance (blue)

---

## 🔐 Role-Based Features

### VIEWER (Personal Finance)
```
✅ Own dashboard
✅ Create transactions
✅ Edit own transactions
✅ Delete own transactions
✅ Filter own data
❌ See other users' data
❌ Analytics
❌ User management
```

### ANALYST (Company Analysis)
```
✅ Company-wide dashboard
✅ View all transactions
✅ Monthly trends
✅ Filter all data
❌ Create/edit/delete transactions
❌ User management
```

### ADMIN (Full Control)
```
✅ Everything ANALYST can do
✅ Create any transaction
✅ Edit any transaction
✅ Delete any transaction
✅ User management
✅ Full system access
```

---

## 🧪 Testing the App

### Test 1: VIEWER Self-Service

1. Login as `viewer` / `viewer`
2. Click "Add Transaction"
3. Create an income record:
   - Amount: 50000
   - Type: Income
   - Category: Salary
   - Date: Today
4. See it appear in dashboard
5. Try editing it - should work ✅
6. Try deleting it - should work ✅

### Test 2: ANALYST Read-Only

1. Login as `analyst` / `analyst`
2. Go to Transactions
3. See all users' transactions
4. Notice no "Add Transaction" button ❌
5. Go to Analytics
6. See monthly trends ✅

### Test 3: ADMIN Full Control

1. Login as `admin` / `admin`
2. Go to Transactions
3. Create a transaction - should work ✅
4. Edit any transaction - should work ✅
5. Delete any transaction - should work ✅
6. Go to Users
7. See all users ✅

---

## 🐛 Common Issues

### Issue: "Network Error"

**Solution**: Backend not running
```bash
# Start backend
cd ../
mvn spring-boot:run
```

### Issue: "401 Unauthorized"

**Solution**: Token expired or invalid
- Logout and login again
- Clear browser localStorage
- Check backend is running

### Issue: "403 Forbidden"

**Solution**: User doesn't have permission
- This is expected behavior
- VIEWER cannot access Analytics
- ANALYST cannot create transactions
- Only ADMIN can manage users

### Issue: Port 3000 already in use

**Solution**: Change port in vite.config.js
```javascript
server: {
  port: 3001  // Change to any available port
}
```

---

## 📦 Project Files

```
finance-frontend/
├── src/
│   ├── components/
│   │   ├── Layout.jsx          # Sidebar navigation
│   │   └── Layout.css
│   ├── context/
│   │   └── AuthContext.jsx     # Auth state management
│   ├── pages/
│   │   ├── Login.jsx           # Login page
│   │   ├── Register.jsx        # Registration
│   │   ├── Dashboard.jsx       # Main dashboard
│   │   ├── Transactions.jsx    # Transaction management
│   │   ├── Analytics.jsx       # Monthly trends
│   │   ├── Users.jsx           # User management
│   │   ├── Auth.css            # Auth pages styles
│   │   ├── Dashboard.css
│   │   ├── Transactions.css
│   │   ├── Analytics.css
│   │   └── Users.css
│   ├── services/
│   │   └── api.js              # API calls
│   ├── App.jsx                 # Main app
│   ├── main.jsx                # Entry point
│   └── index.css               # Global styles
├── index.html
├── vite.config.js              # Vite config
├── package.json
├── README.md
└── SETUP.md (this file)
```

---

## 🎯 Next Steps

1. **Customize**: Update colors, fonts, or layout
2. **Add Features**: Implement additional functionality
3. **Deploy**: Build and deploy to production
4. **Test**: Add unit tests with Vitest
5. **Optimize**: Add code splitting and lazy loading

---

## 💡 Tips

- Use Chrome DevTools to inspect API calls
- Check Network tab for API responses
- Use React DevTools to debug components
- Check Console for errors
- Use localStorage to inspect JWT token

---

## 🎉 You're All Set!

Your Finance Frontend is now running and connected to the backend. Start tracking your finances or analyzing company data based on your role!

**Happy Coding! 💰**
