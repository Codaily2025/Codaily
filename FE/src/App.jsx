import React, { useState, useEffect, useRef } from 'react';
import { BrowserRouter as Router, Routes, Route, useNavigate, useLocation } from 'react-router-dom';
import './App.css';
import NavBar from './components/NavBar';
import Home from './pages/Home/Home';
import Schedule from './pages/Schedule/Schedule';
import Project from './pages/Project/Project';
import ProjectCreate from './pages/ProjectCreate/ProjectCreate';
import History from './pages/History/History';
import MyPage from './pages/MyPage/MyPage';
import Login from './pages/Login/Login';
import Signup from './pages/Signup/Signup';
import ModalManager from './components/organisms/ModalManager';

// 보호된 라우트 컴포넌트 : 로그인 상태가 아니면 접근 불가
const ProtectedRoute = ({ children, isLoggedIn, onRedirectToLogin }) => { 

  const navigate = useNavigate();
  const redirectedRef = useRef(false);

  useEffect(() => { // useEffet훅 : 컴포넌트가 마운트될 때 실행되는 함수
    if (!isLoggedIn && !redirectedRef.current) {
      redirectedRef.current = true;
      onRedirectToLogin(); // 알림 + navigate('/login')
    }
  }, [isLoggedIn, onRedirectToLogin, navigate]); // 의존성 배열 : 특정 값이 변경될 때만 실행
  
  if (!isLoggedIn) {
    return null; // children을 렌더링하지 않음
  }
  return children;
};


function AppContent() {
  const [isLoggedIn, setIsLoggedIn] = useState(false); // 기본값을 false로 변경
  const location = useLocation();
  const navigate = useNavigate();
  
  // 현재 경로에 따라 활성 메뉴 결정
  const getActiveMenu = () => {
    switch (location.pathname) {
      case '/schedule':
        return '일정';
      case '/project':
        return '프로젝트';
      case '/history':
        return '히스토리';
      case '/mypage':
        return '';
      case '/login':
        return '';
      default:
        return '';
    }
  };

  const handleMenuClick = (menuItem) => {
    switch (menuItem) {
      case '일정':
        navigate('/schedule');
        break;
      case '프로젝트':
        navigate('/project');
        break;
      case '히스토리':
        navigate('/history');
        break;
      default:
        break;
    }
  };

  const handleLogoClick = () => {
    navigate('/');
  };

  const handleMyPageClick = () => {
    navigate('/mypage');
  };

  const handleLogoutClick = () => {
    setIsLoggedIn(false);
    navigate('/');
  };

  const handleLogin = () => {
    setIsLoggedIn(true);
    navigate('/project'); // 로그인 후 프로젝트 페이지로 이동
  };

  const handleRedirectToLogin = () => {
    alert('로그인이 필요한 페이지입니다. 로그인 페이지로 이동합니다.');
    navigate('/login');
  };

  const handleLoginClick = () => {
    navigate('/login');
  };

  return (
    <div className="App">
      <NavBar 
        // isLoggedIn={isLoggedIn} 
        isLoggedIn={true} 
        activeMenu={getActiveMenu()} 
        onMenuClick={handleMenuClick}
        onLogoClick={handleLogoClick}
        onMyPageClick={handleMyPageClick}
        onLogoutClick={handleLogoutClick}
        onLoginClick={handleLoginClick}
      />
      <main className="main-content">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login onLogin={handleLogin} />} />
          
          {/* 보호된 라우트들 : 로그인 상태가 아니면 접근 불가 */}
          <Route 
            path="/schedule" 
            element={
              // <ProtectedRoute isLoggedIn={isLoggedIn} onRedirectToLogin={handleRedirectToLogin}>
              <ProtectedRoute isLoggedIn={true} onRedirectToLogin={handleRedirectToLogin}>
                <Schedule />
              </ProtectedRoute>
            } 
          />
          {/* TODO : 페이지 확인 후 삭제하기 */}
          <Route 
            path="/signup" 
            element={
              // <ProtectedRoute isLoggedIn={isLoggedIn} onRedirectToLogin={handleRedirectToLogin}>
              <ProtectedRoute isLoggedIn={true} onRedirectToLogin={handleRedirectToLogin}>
                <Signup />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/project" 
            element={
              <ProtectedRoute isLoggedIn={isLoggedIn} onRedirectToLogin={handleRedirectToLogin}>
                <Project />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/project/create"
            element={
              <ProtectedRoute isLoggedIn={isLoggedIn} onRedirectToLogin={handleRedirectToLogin}>
                <ProjectCreate />
              </ProtectedRoute>
            }
          />
          <Route 
            path="/history" 
            element={
              <ProtectedRoute isLoggedIn={isLoggedIn} onRedirectToLogin={handleRedirectToLogin}>
                <History />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/mypage" 
            element={
              <ProtectedRoute isLoggedIn={isLoggedIn} onRedirectToLogin={handleRedirectToLogin}>
                <MyPage />
              </ProtectedRoute>
            } 
          />
        </Routes>
      </main>
    </div>
  );
}

function App() {
  return (
    <Router>
      <AppContent />

      {/* 전역 Modal Manager */}
      <ModalManager />
    </Router>
  );
}

export default App;
