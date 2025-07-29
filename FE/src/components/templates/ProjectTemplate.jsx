import React from 'react';
import Calendar from '../organisms/Calendar'

const ProjectTemplate = () => {
  return (
    <div>
      {/* 상단 영역 - 2개 컬럼 */}
      <div style={{ 
        display: 'grid', 
        gridTemplateColumns: '2fr 1fr', 
        gap: '20px',
        marginBottom: '20px'
      }}>
        {/* 첫 번째 영역 - 일정별 호출 */}
        <div style={{
        //   border: '2px solid #333',
        //   boxShadow: '0px 1px 2px rgba(0, 0, 0, 0.3), 0px 2px 6px 2px rgba(0, 0, 0, 0.15)',
          boxShadow: '1px 1px 1px rgba(0, 0, 0, 0.25)',
          borderRadius: '16px',
          padding: '20px',
          backgroundColor: '#FAF9FD',
          minHeight: '400px'
        }}>
          <h2 style={{ margin: '0 0 20px 0', fontSize: '18px', fontWeight: 'bold' }}>
            일정별 호출 : 월별? 주별? ??
          </h2>
          {/* 내용은 비워둠 */}
          <Calendar />
        </div>

        {/* 두 번째 영역 - 오늘 할 일 */}
        <div style={{
        //   border: '2px solid #333',
        // boxShadow: '0px 1px 2px rgba(0, 0, 0, 0.3), 0px 2px 6px 2px rgba(0, 0, 0, 0.15)',
        boxShadow: '1px 1px 1px rgba(0, 0, 0, 0.25)',
          borderRadius: '16px',
          padding: '20px',
          backgroundColor: '#FAF9FD',
          minHeight: '400px'
        }}>
          <h2 style={{ margin: '0 0 20px 0', fontSize: '18px', fontWeight: 'bold' }}>
            오늘 할 일
          </h2>
          {/* 내용은 비워둠 */}
        </div>
      </div>

      {/* 세 번째 영역 - 칸반 보드 */}
      <div style={{
        // border: '2px solid #333',
        // boxShadow: '0px 1px 2px rgba(0, 0, 0, 0.3), 0px 2px 6px 2px rgba(0, 0, 0, 0.15)',
        boxShadow: '1px 1px 1px rgba(0, 0, 0, 0.25)',
        borderRadius: '16px',
        padding: '20px',
        backgroundColor: '#FAF9FD',
        minHeight: '300px'
      }}>
        <h2 style={{ margin: '0 0 20px 0', fontSize: '18px', fontWeight: 'bold' }}>
          DB 구축 | 로그인 | 회원가입
        </h2>
        {/* 내용은 비워둠 */}
      </div>
    </div>
  );
};

export default ProjectTemplate;