import React from 'react';
import './Login.css';
import { useNavigate } from 'react-router-dom';
import googleIcon from '../../assets/google_icon.png';
import kakaoIcon from '../../assets/kakao_icon.png';
import naverIcon from '../../assets/naver_icon.png';
import mainImage from '../../assets/main_image.png';
import logoInImage from '../../assets/logo_in_image.png';

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