import { useState, useEffect } from 'react';

export default function StatusChecker() {
  const [isActive, setIsActive] = useState(false);

  useEffect(() => {
    const checkStatus = () => {
      const active = Math.random() > 0.5;
      setIsActive(active);
    };

    checkStatus();
    const interval = setInterval(checkStatus, 5000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div style={{
      padding: '20px',
      backgroundColor: isActive ? '#4ade80' : '#ef4444',
      color: 'white',
      borderRadius: '8px',
      textAlign: 'center'
    }}>
      {isActive ? 'Active' : 'Not Active'}
    </div>
  );
}