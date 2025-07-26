import React from 'react';
import './Login.css';
import { useNavigate } from 'react-router-dom';

const Login = ({ onLogin }) => {
  const navigate = useNavigate();

  const handleSocialLogin = (provider) => {
    // 실제로는 여기서 각 소셜 로그인 API를 호출합니다
    console.log(`${provider} 로그인 시도`);
    
    // 임시로 바로 로그인 처리
    if (onLogin) {
      onLogin();
    }
    navigate('/'); // 로그인 후 프로젝트 페이지로 이동
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <h1>로그인</h1>
        <p className="login-subtitle">소셜 계정으로 간편하게 로그인하세요</p>
        
        <div className="social-login-buttons">
          <button 
            className="social-login-button google"
            onClick={() => handleSocialLogin('Google')}
          >
            <div className="social-icon google-icon">G</div>
            <span>Google로 로그인</span>
          </button>
          
          <button 
            className="social-login-button kakao"
            onClick={() => handleSocialLogin('Kakao')}
          >
            <div className="social-icon kakao-icon">K</div>
            <span>Kakao로 로그인</span>
          </button>
          
          <button 
            className="social-login-button naver"
            onClick={() => handleSocialLogin('Naver')}
          >
            <div className="social-icon naver-icon">N</div>
            <span>Naver로 로그인</span>
          </button>
        </div>
      </div>
    </div>
  );
};

export default Login; 