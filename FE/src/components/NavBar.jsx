import React from 'react';
import styles from './NavBar.module.css';
import logo from '../assets/codaily.svg';

const NavBar = ({ isLoggedIn, activeMenu = '프로젝트', onMenuClick, onLogoClick, onMyPageClick, onLogoutClick, onLoginClick }) => {
  const menuItems = ['일정', '프로젝트', '히스토리'];

  const handleMenuClick = (item) => {
    if (onMenuClick) {
      onMenuClick(item);
    }
  };

  const handleLogoClick = () => {
    if (onLogoClick) {
      onLogoClick();
    }
  };

  const handleMyPageClick = () => {
    if (onMyPageClick) {
      onMyPageClick();
    }
  };

  const handleLogoutClick = () => {
    if (onLogoutClick) {
      onLogoutClick();
    }
  };

  const handleLoginClick = () => {
    if (onLoginClick) {
      onLoginClick();
    }
  };

  return (
    <div className={styles.navWrapper}>
      <div className={styles.background} />

      {/* 로고 */}
      <div className={styles.logoContainer} onClick={handleLogoClick} style={{ cursor: 'pointer' }}>
        <img src={logo} alt="logo" className={styles.logoImage} />
        <div className={styles.logoText}>odaily</div>
      </div>

      {/* 메뉴 */}
      <div className={styles.menuContainer}>
        {menuItems.map((item) => (
          <div
            key={item}
            className={`${styles.menuItem} ${
              item === activeMenu ? styles.active : ''
            }`}
            onClick={() => handleMenuClick(item)}
            style={{ cursor: 'pointer' }}
          >
            {item}
          </div>
        ))}
      </div>

      {/* 활성화된 메뉴 하단 밑줄 */}
      {activeMenu && (
        <div
          className={styles.activeUnderline}
          style={{
            left:
              activeMenu === '일정'
                ? 590
                : activeMenu === '프로젝트'
                ? 666
                : 759,
          }}
        />
      )}

      {/* 우측 섹션 */}
      {isLoggedIn ? (
        <>
          <div className={styles.myPage} onClick={handleMyPageClick} style={{ cursor: 'pointer' }}>마이페이지</div>
          <div className={styles.logoutButton} onClick={handleLogoutClick} style={{ cursor: 'pointer' }}>
            <span>로그아웃</span>
          </div>
        </>
      ) : (
        <>
          <div className={styles.signUp}>회원가입</div>
          <div className={styles.loginButton} onClick={handleLoginClick} style={{ cursor: 'pointer' }}>
            <span>로그인</span>
          </div>
        </>
      )}
    </div>
  );
};

export default NavBar;
