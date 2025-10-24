import React from 'react';
import './App.css';

function App() {
  return (
    <div className="app-container">
      {/* Header */}
      <header className="header">
        <div className="logo">
          <h1>MyLogo</h1>
        </div>
        <div className="login">
          <button className="login-btn">Login</button>
        </div>
      </header>

      {/* Main Content */}
      <main className="main-content">
        <h2>Welcome to Our Website</h2>
        <p>This is the main content area where your page content goes.</p>
        <p>You can add more sections, components, and features here.</p>
      </main>

      {/* Footer */}
      <footer className="footer">
        <p>&copy; 2025 MyWebsite. All rights reserved.</p>
      </footer>
    </div>
  );
}

export default App;
