// src/pages/OAuthCallback.jsx
import React, { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';
import { authInstance } from '../apis/axios';

const OAuthCallback = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  // const { setToken } = useAuthStore();
  const { setToken, setUser } = useAuthStore();

  // // 사용자 정보를 가져오는 함수
  // const fetchUserInfo = async () => {
  //   try {
  //     const response = await authInstance.get('/api/auth/me');
  //     const userData = response.data;
      
  //     if (userData.authenticated) {
  //       // 사용자 정보를 스토어에 저장
  //       setUser({
  //         userId: userData.userId,
  //         email: userData.email,
  //         nickname: userData.nickname,
  //         provider: userData.provider
  //       });
  //       console.log('User info saved:', userData);
  //     }
  //   } catch (error) {
  //     console.error('Failed to fetch user info:', error);
  //   }
  // };

  useEffect(() => {
    const token = searchParams.get('token');
    const error = searchParams.get('error');
    
    if (error) {
      console.error('OAuth login error:', error);
      alert('로그인 중 오류가 발생했습니다. 다시 시도해주세요.');
      navigate('/login', { replace: true });
      return;
    }
    
    if (token) {
      // 토큰을 스토어에 저장
      setToken(token);
      console.log('OAuth login successful, token saved');
      
      // 사용자 정보 가져오기
      // fetchUserInfo().then(() => {
        // 로그인 성공 후 홈페이지로 리다이렉트
        navigate('/', { replace: true });
      // });
    } else {
      console.log('No token received, redirecting to login');
      // 토큰이 없으면 로그인 페이지로 리다이렉트
      navigate('/login', { replace: true });
    }
  }, [searchParams, setToken, navigate]);
  // }, [searchParams, setToken, setUser, navigate]);

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
