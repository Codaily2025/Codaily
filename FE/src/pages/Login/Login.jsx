import React from 'react';
import './Login.css';
import { useNavigate } from 'react-router-dom';
import googleIcon from '../../assets/google_icon.png';
import kakaoIcon from '../../assets/kakao_icon.png';
import naverIcon from '../../assets/naver_icon.png';
import mainImage from '../../assets/main_image.png';
import logoInImage from '../../assets/logo_in_image.png';

import { useMutation } from '@tanstack/react-query';
import { defaultInstance } from '../../apis/axios2';
import { useAuthStore } from '../../stores/authStore';

const Login = ({ onLogin }) => {
  const navigate = useNavigate();

  const handleSocialLogin = (provider) => {
    if (provider === 'google') {
      // 구글 OAuth 로그인 - 백엔드 OAuth 엔드포인트로 리다이렉트
      window.location.href = `${import.meta.env.VITE_BASE_URL}oauth2/authorization/google`;
    } else if (provider === 'naver') {
      // 네이버 OAuth 로그인
      window.location.href = `${import.meta.env.VITE_BASE_URL}oauth2/authorization/naver`;
    } else {
      // 카카오는 아직 구현되지 않음
      console.log(`${provider} 로그인은 아직 구현되지 않았습니다.`);
    }
  };

  return (
    <div className="login-page-container">
      {/* 왼쪽 로그인 영역 */}
      <div className="login-form-section">
        <h1 className="welcome-heading">Welcome to Codaily</h1>
        <p className="login-subheading">간편하게 시작하세요</p>

        <div className="social-login-buttons">
          <button className="social-login-button" onClick={() => handleSocialLogin('google')}>
            <img src={googleIcon} alt="Google logo" />
            <span>Start with Google</span>
          </button>
          <button className="social-login-button" onClick={() => handleSocialLogin('kakao')}>
            <img src={kakaoIcon} alt="Kakao logo" />
            <span>Start with Kakao</span>
          </button>
          <button className="social-login-button" onClick={() => handleSocialLogin('naver')}>
            <img src={naverIcon} alt="Naver logo" />
            <span>Start with Naver</span>
          </button>
        </div>
      </div>

      {/* 오른쪽 이미지 영역 */}
      <div className="login-image-section">
        <img src={mainImage} className="login-background-image" alt="Decorative background" />
        <img src={logoInImage} className="foreground-image" alt="Codaily logo" />
      </div>
    </div>
  );
};

export default Login; 