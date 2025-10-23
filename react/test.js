import { useState, useEffect } from 'react';

function StatusChecker() {
  const [isActive, setIsActive] = useState(false);

  useEffect(() => {
    // Function to check the status
    const checkStatus = () => {
      // Your logic to determine if active or not
      // For example, checking an API or some condition:
      const active = Math.random() > 0.5; // Random for demo
      setIsActive(active);
    };

    // Check immediately on mount
    checkStatus();

    // Set up interval to check every 5 seconds
    const interval = setInterval(checkStatus, 5000);

    // Cleanup interval on unmount
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

export default StatusChecker;
Key points:

setInterval runs your check function every 5000ms (5 seconds)
Cleanup function in useEffect clears the interval when component unmounts to prevent memory leaks
Color styling uses green (#4ade80) for active and red (#ef4444) for inactive

If you're checking an external source (like an API), you'd replace the random logic with your actual check:
jsxconst checkStatus = async () => {
  const response = await fetch('/api/status');
  const data = await response.json();
  setIsActive(data.isActive);
};