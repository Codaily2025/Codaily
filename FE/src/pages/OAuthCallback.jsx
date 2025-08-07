// src/pages/OAuthCallback.jsx
import React, { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';

const OAuthCallback = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { setToken } = useAuthStore();

  useEffect(() => {
    const token = searchParams.get('token');
    
    if (token) {
      // 토큰을 스토어에 저장
      setToken(token);
      
      // 로그인 성공 후 홈페이지로 리다이렉트
      navigate('/', { replace: true });
    } else {
      // 토큰이 없으면 로그인 페이지로 리다이렉트
      navigate('/login', { replace: true });
    }
  }, [searchParams, setToken, navigate]);

  return (
    <div style={{ 
      display: 'flex', 
      justifyContent: 'center', 
      alignItems: 'center', 
      height: '100vh',
      flexDirection: 'column'
    }}>
      <div>로그인 처리 중...</div>
      <div style={{ marginTop: '20px' }}>
        <div className="spinner"></div>
      </div>
      <style>{`
        .spinner {
          width: 40px;
          height: 40px;
          border: 4px solid #f3f3f3;
          border-top: 4px solid #3498db;
          border-radius: 50%;
          animation: spin 1s linear infinite;
        }
        
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
};

export default OAuthCallback;
