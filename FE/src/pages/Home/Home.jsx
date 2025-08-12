import React from 'react';
import './Home.css';
import home_background from '../../assets/home_background.png';
import { useNavigate } from 'react-router-dom';
import { useUserQuery } from '../../queries/useUser';

const Home = () => {
  const navigate = useNavigate();
  const { data: user } = useUserQuery();
  console.log('Home 페이지 사용자 정보:', user);
  return (
    <div className="home-container">
      <img
        className="home-bg-image"
        src={home_background}
        alt="배경 이미지"
      />
      <div className="hero-section">
        <h1 className="hero-title">
          Daily Code. <br />
          Daily Progress.
        </h1>
        <p className="hero-subtitle">
          하루하루 쌓이는 개발 경험. <br />
          Codaily로 프로젝트 기획부터 완성까지 스마트하게.
        </p>
        <button className="home-create-project-button" onClick={() => navigate('/project/create')}>
          프로젝트 생성하기
        </button>
      </div>
      {/* 이미지는 디자인의 일부로 보여 CSS 배경으로 처리하거나, 
          필요하다면 여기에 img 태그를 추가할 수 있습니다. */}
    </div>
  );
};

export default Home; 