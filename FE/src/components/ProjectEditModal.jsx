import React from 'react';

/* 프로젝트 수정 모달 스타일 */
const ProjectEditModalStyles = () => (
  <style>{`
    /* 모달 오버레이 */
    .modal-overlay {
      position: fixed;
      inset: 0;
      z-index: 1000;
      background: rgba(0, 0, 0, 0.4); /* 어두운 배경 */
      display: flex;
      justify-content: center;
      align-items: center;
    }

    /* 모달 컨테이너 */
    .modal-container {
      width: 463px;
      height: 760px;
      position: relative;
      background: white;
      box-shadow: 0px 8px 8px -4px rgba(10, 13, 18, 0.04);
      overflow: hidden;
      border-radius: 16px;
      display: flex;
      flex-direction: column;
    }

    /* 모달 헤더 */
    .modal-header-wrapper {
      width: 100%;
      position: absolute;
      top: 0;
      left: 0;
      background: white;
      display: flex;
      flex-direction: column;
      align-items: center;
    }

    .modal-header {
      align-self: stretch;
      padding: 24px;
      background: white;
      display: flex;
      flex-direction: column;
      justify-content: flex-start;
      align-items: flex-start;
      gap: 16px;
    }

    .modal-header-text {
      align-self: stretch;
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .modal-title {
      align-self: stretch;
      color: #D690A6;
      font-size: 18px;
      font-weight: 600;
      line-height: 28px;
    }

    .modal-subtitle {
      align-self: stretch;
      color: #525252;
      font-size: 14px;
      font-weight: 400;
      line-height: 20px;
    }

    .close-button-wrapper {
      padding: 10px;
      position: absolute;
      left: 403px;
      top: 16px;
      border-radius: 8px;
      overflow: hidden;
      display: inline-flex;
      justify-content: center;
      align-items: center;
      cursor: pointer;
    }

    .modal-header-spacer {
      align-self: stretch;
      height: 20px;
    }

    /* 모달 본문 */
    .modal-body {
      width: 100%;
      padding: 0 24px;
      position: absolute;
      top: 96px;
      display: inline-flex;
      flex-direction: column;
      justify-content: flex-start;
      align-items: flex-start;
      gap: 20px;
      box-sizing: border-box;
    }

    .form-container {
      width: 100%;
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .form-section {
      align-self: stretch;
      display: flex;
      flex-direction: column;
      gap: 6px;
    }

    .form-label {
      color: #404040;
      font-size: 14px;
      font-weight: 500;
      line-height: 20px;
    }

    .input-wrapper {
      align-self: stretch;
      padding: 10px 14px;
      background: white;
      box-shadow: 0px 1px 2px rgba(10, 13, 18, 0.05);
      border-radius: 8px;
      outline: 1px #D5D7DA solid;
      outline-offset: -1px;
      display: inline-flex;
      justify-content: flex-start;
      align-items: center;
      gap: 8px;
    }

    .input-text {
      flex: 1 1 0;
      color: #737373;
      font-size: 16px;
      font-weight: 400;
      line-height: 24px;
    }

    .input-with-icon {
      flex: 1 1 0;
      display: flex;
      justify-content: flex-start;
      align-items: center;
      gap: 8px;
    }

    .date-range-wrapper {
      align-self: stretch;
      display: inline-flex;
      justify-content: flex-start;
      align-items: center;
      gap: 6px;
    }

    .date-input {
      width: 193px;
    }

    .date-range-separator {
      height: 44px;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      color: #6B7280;
      font-size: 16px;
    }

    /* 요일별 투자 시간 */
    .investment-section {
        align-items: flex-start;
        gap: 8px;
    }

    .day-selector {
      align-self: stretch;
      padding: 0 55px;
      display: inline-flex;
      justify-content: flex-start;
      align-items: flex-start;
      gap: 8px;
      flex-wrap: wrap;
    }

    .day-chip {
      width: 36px;
      height: 36px;
      padding: 16px 8px 17px 8px;
      border-radius: 12px;
      display: inline-flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      text-align: center;
      font-size: 16px;
      font-weight: 700;
      box-sizing: border-box;
      cursor: pointer;
    }

    .day-chip.inactive {
      background: white;
      outline: 2px #E8BBBA solid;
      outline-offset: -2px;
      color: #D690A6;
    }

    .day-chip.active {
      background: linear-gradient(135deg, #EFDBDA 0%, #DFA5B0 100%);
      box-shadow: 0px 2px 8px rgba(0, 0, 0, 0.15);
      color: white;
      padding-left: 6px;
      padding-right: 6px;
    }

    .day-chip.disabled {
      background: white;
      outline: 2px #E5E5E5 solid;
      outline-offset: -2px;
      color: #D4D4D4;
    }

    .slider-wrapper {
      align-self: stretch;
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .slider-container {
        width: 340px;
        height: 10px;
        position: relative;
        background: #F5F5F5;
        border-radius: 4px;
        margin-top: 8px;
        margin-left: auto;
        margin-right: auto;
    }
    .slider-track {
        width: 100%;
        height: 100%;
        position: relative;
        border-radius: 4px;
    }
    .slider-progress {
        height: 10px;
        left: 0;
        top: 0;
        position: absolute;
        background: #D690A6;
        border-radius: 4px;
    }
    .slider-thumb {
        top: -6px;
        position: absolute;
    }
    .slider-text-wrapper { 
      width: 100%;     
      display: flex;
      justify-content: flex-end;
    }
    .slider-text {
      color: #737373;
      margin-right: 40px;
      margin-top: 7px;
      font-size: 15px;
      font-weight: 500;
      line-height: 20px;
    }


    /* Github 연결 */
    .repo-options {
      align-self: stretch;
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .repo-option-card {
      align-self: stretch;
      padding: 16px;
      border-radius: 12px;
      display: inline-flex;
      justify-content: flex-start;
      align-items: flex-start;
      gap: 4px;
      cursor: pointer;
    }

    .repo-option-card.active {
      background: rgba(232, 187, 186, 0.15);
      outline: 2px #DFA5B0 solid;
      outline-offset: -2px;
    }

    .repo-option-card:not(.active) {
      background: white;
      outline: 1px #E9EAEB solid;
      outline-offset: -1px;
    }

    .repo-option-content {
      flex: 1 1 0;
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 16px;
    }

    .repo-icon-wrapper {
      width: 32px;
      height: 32px;
      position: relative;
      mix-blend-mode: multiply;
      background: #EFDBDA;
      border-radius: 28px;
      outline: 4px #F5EFEE solid;
      outline-offset: -2px;
      display: flex;
      justify-content: center;
      align-items: center;
    }

    .repo-text-content {
      flex: 1 1 0;
      display: flex;
      flex-direction: column;
    }

    .repo-title {
      color: #CD7B9C;
      font-size: 14px;
      font-weight: 500;
      line-height: 20px;
    }

    .repo-url {
      align-self: stretch;
      color: #CD7B9C;
      font-size: 14px;
      font-weight: 400;
      line-height: 20px;
    }

    .repo-title-dark {
        color: #414651;
        font-size: 14px;
        font-weight: 500;
        line-height: 20px;
    }

    .radio-check-wrapper {
      width: 16px;
      height: 16px;
      position: relative;
      border-radius: 50%;
    }

    .radio-check-wrapper:not(.checked) {
        background: white;
        border: 1px solid #D5D7DA;
    }

    .radio-check-wrapper.checked {
      background: #D690A6;
      border: 1px solid #D690A6;
      display: flex;
      justify-content: center;
      align-items: center;
    }

    /* 모달 푸터 */
    .modal-footer {
      width: 100%;
      position: absolute;
      top: 660px;
      left: 0;
      padding-top: 32px;
    }

    .button-group {
      padding: 0 24px 24px 24px;
      display: inline-flex;
      justify-content: flex-start;
      align-items: flex-start;
      gap: 12px;
      width: 100%;
      box-sizing: border-box;
    }

    .btn {
      flex: 1 1 0;
      padding: 10px 18px;
      box-shadow: 0px 1px 2px rgba(10, 13, 18, 0.05);
      border-radius: 8px;
      justify-content: center;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 600;
      line-height: 24px;
      border: none;
      cursor: pointer;
    }

    .btn-secondary {
      background: white;
      color: #404040;
      outline: 1px #D5D7DA solid;
      outline-offset: -1px;
    }

    .btn-primary {
      background: #D690A6;
      color: white;
      outline: 1px #DFA5B0 solid;
      outline-offset: -1px;
    }
  `}</style>
);


// SVG 아이콘들
const CloseIcon = () => (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path d="M18 6L6 18M6 6L18 18" stroke="#737373" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
  </svg>
);

const CalendarIcon = () => (
  <svg width="20" height="22" viewBox="0 0 20 22" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path fillRule="evenodd" clipRule="evenodd" d="M6.90698 21.9651C1.15698 21.9651 0 19.2591 0 14.8871V8.58707C0 4.03607 1.44702 2.02206 5.11603 1.60208V0.775085C5.1311 0.362122 5.47028 0.0351562 5.88348 0.0351562C6.29669 0.0351562 6.63586 0.362122 6.65094 0.775085L6.651 1.54108H13.302V0.775085C13.3171 0.362122 13.6563 0.0351562 14.0695 0.0351562C14.4827 0.0351562 14.8219 0.362122 14.837 0.775085V1.60208C18.505 2.02209 19.953 4.03607 19.953 8.58707V14.8871C19.953 19.2581 18.797 21.9651 13.046 21.9651H6.90698ZM6.90698 20.4321H13.047C16.479 20.4321 17.775 19.5321 18.216 17.4711H1.73798C2.17896 19.5331 3.47498 20.432 6.90698 20.432V20.4321ZM18.396 15.9391C18.412 15.6081 18.419 15.2581 18.419 14.8871V8.58707C18.419 4.56805 17.213 3.44107 14.838 3.14206V3.84207C14.8229 4.25504 14.4837 4.582 14.0705 4.582C13.6573 4.582 13.3181 4.25504 13.303 3.84207V3.07608H6.65204V3.84207C6.63696 4.25504 6.29779 4.582 5.88458 4.582C5.47137 4.582 5.1322 4.25504 5.11713 3.84207L5.11707 3.14206C2.75507 3.43707 1.53607 4.55307 1.53607 8.58905V14.889C1.53607 15.26 1.54309 15.611 1.55908 15.941L18.396 15.9391Z" fill="#737373" />
  </svg>
);

const FolderIcon = () => (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M2.66634 13.3332C2.29967 13.3332 1.98579 13.2026 1.72467 12.9415C1.46356 12.6804 1.33301 12.3665 1.33301 11.9998V3.99984C1.33301 3.63317 1.46356 3.31928 1.72467 3.05817C1.98579 2.79706 2.29967 2.6665 2.66634 2.6665H6.66634L7.99967 3.99984H13.333C13.6997 3.99984 14.0136 4.13039 14.2747 4.3915C14.5358 4.65261 14.6663 4.9665 14.6663 5.33317V11.9998C14.6663 12.3665 14.5358 12.6804 14.2747 12.9415C14.0136 13.2026 13.6997 13.3332 13.333 13.3332H2.66634ZM2.66634 11.9998H13.333V5.33317H7.44967L6.11634 3.99984H2.66634V11.9998Z" fill="#D690A6"/>
    </svg>
);

const CheckIcon = () => (
    <svg width="10" height="10" viewBox="0 0 10 10" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M8.33366 2.5L3.75033 7.08333L1.66699 5" stroke="white" strokeWidth="1.66667" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
);

const AddIcon = () => (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M7.33301 11.3335H8.66634V8.66683H11.333V7.3335H8.66634V4.66683H7.33301V7.3335H4.66634V8.66683H7.33301V11.3335ZM7.99967 14.6668C7.07745 14.6668 6.21079 14.4946 5.39967 14.1502C4.58856 13.7946 3.88301 13.3168 3.28301 12.7168C2.68301 12.1168 2.20523 11.4113 1.84967 10.6002C1.50523 9.78905 1.33301 8.92238 1.33301 8.00016C1.33301 7.07794 1.50523 6.21127 1.84967 5.40016C2.20523 4.58905 2.68301 3.8835 3.28301 3.2835C3.88301 2.6835 4.58856 2.21127 5.39967 1.86683C6.21079 1.51127 7.07745 1.3335 7.99967 1.3335C8.9219 1.3335 9.78856 1.51127 10.5997 1.86683C11.4108 2.21127 12.1163 2.6835 12.7163 3.2835C13.3163 3.8835 13.7886 4.58905 14.133 5.40016C14.4886 6.21127 14.6663 7.07794 14.6663 8.00016C14.6663 8.92238 14.4886 9.78905 14.133 10.6002C13.7886 11.4113 13.3163 12.1168 12.7163 12.7168C12.1163 13.3168 11.4108 13.7946 10.5997 14.1502C9.78856 14.4946 8.9219 14.6668 7.99967 14.6668ZM7.99967 13.3335C9.48856 13.3335 10.7497 12.8168 11.783 11.7835C12.8163 10.7502 13.333 9.48905 13.333 8.00016C13.333 6.51127 12.8163 5.25016 11.783 4.21683C10.7497 3.1835 9.48856 2.66683 7.99967 2.66683C6.51079 2.66683 5.24967 3.1835 4.21634 4.21683C3.18301 5.25016 2.66634 6.51127 2.66634 8.00016C2.66634 9.48905 3.18301 10.7502 4.21634 11.7835C5.24967 12.8168 6.51079 13.3335 7.99967 13.3335Z" fill="#D690A6"/>
    </svg>
);

const RepoIcon = () => (
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
        <g clipPath="url(#clip0_461_4176)">
        <path d="M1.33301 7.99988L7.76116 11.214C7.84862 11.2577 7.89234 11.2795 7.93821 11.2882C7.97883 11.2958 8.02052 11.2958 8.06114 11.2882C8.10701 11.2795 8.15073 11.2577 8.23819 11.214L14.6663 7.99988M1.33301 11.3332L7.76116 14.5473C7.84862 14.591 7.89234 14.6129 7.93821 14.6215C7.97883 14.6291 8.02052 14.6291 8.06114 14.6215C8.10701 14.6129 8.15073 14.591 8.23819 14.5473L14.6663 11.3332M1.33301 4.66655L7.76116 1.45247C7.84862 1.40874 7.89234 1.38688 7.93821 1.37827C7.97883 1.37065 8.02052 1.37065 8.06114 1.37827C8.10701 1.38688 8.15073 1.40874 8.23819 1.45247L14.6663 4.66655L8.23819 7.88062C8.15073 7.92435 8.10701 7.94621 8.06114 7.95482C8.02052 7.96244 7.97883 7.96244 7.93821 7.95482C7.89234 7.94621 7.84862 7.92435 7.76116 7.88062L1.33301 4.66655Z" stroke="#D690A6" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
        </g>
        <defs>
        <clipPath id="clip0_461_4176">
        <rect width="16" height="16" fill="white"/>
        </clipPath>
        </defs>
    </svg>
);


const ProjectEditModal = ({ onClose, project, onSave }) => {
  const days = ['월', '화', '수', '목', '금', '토', '일'];
  const [selectedRepoOption, setSelectedRepoOption] = React.useState(0); // 0: 현재, 1: 새로 만들기, 2: 기존 연결

  return (
    <>
      <ProjectEditModalStyles />
      {/* 중앙 정렬용 래퍼 */}
      <div className="modal-overlay" onClick={onClose}>
        <div className="modal-container"  onClick={(e) => e.stopPropagation()}>
          <div className="modal-header-wrapper">
            <div className="modal-header">
              <div className="modal-header-text">
                <div className="modal-title">프로젝트 설정</div>
                <div className="modal-subtitle">프로젝트에 대한 기본 정보를 수정할 수 있어요.</div>
              </div>
            </div>
            <div className="close-button-wrapper" onClick={onClose}>
              <CloseIcon />
            </div>
            <div className="modal-header-spacer" />
          </div>

          <div className="modal-body">
            <div className="form-container">
              <div className="form-section">
                <label className="form-label">프로젝트명 *</label>
                <div className="input-wrapper">
                  <div className="input-text">샘플 프로젝트</div>
                </div>
              </div>

              <div className="form-section">
                <label className="form-label">기간*</label>
                <div className="date-range-wrapper">
                  <div className="input-wrapper date-input">
                    <div className="input-with-icon">
                      <CalendarIcon />
                      <div className="input-text">2025/04/03</div>
                    </div>
                  </div>
                  <div className="date-range-separator">~</div>
                  <div className="input-wrapper date-input">
                    <div className="input-with-icon">
                      <CalendarIcon />
                      <div className="input-text">2025/04/03</div>
                    </div>
                  </div>
                </div>
              </div>
              
              <div className="form-section investment-section">
                <label className="form-label">요일별 투자 시간*</label>
                <div className="day-selector">
                    {days.map((day, index) => (
                        <div key={day} className={`day-chip ${day === '금' ? 'active' : (index > 4 ? 'disabled' : 'inactive')}`}>
                            {day}
                        </div>
                    ))}
                </div>
                <div className="slider-wrapper">
                  <div className="slider-container">
                      <div className="slider-track">
                          <div className="slider-progress" style={{width: '70%'}}></div>
                          <div className="slider-thumb" style={{left: 'calc(70% - 15px)'}}>
                              <svg width="30" height="30" viewBox="0 0 30 30" fill="none" xmlns="http://www.w3.org/2000/svg">
                                  <g filter="url(#slider-thumb-shadow)">
                                  <rect x="1" y="1" width="22" height="22" rx="11" fill="#F5F5F5"/>
                                  </g>
                                  <defs>
                                  <filter id="slider-thumb-shadow" x="0" y="0" width="30" height="30" filterUnits="userSpaceOnUse" colorInterpolationFilters="sRGB">
                                  <feFlood floodOpacity="0" result="BackgroundImageFix"/>
                                  <feColorMatrix in="SourceAlpha" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0" result="hardAlpha"/>
                                  <feOffset dx="3" dy="3"/>
                                  <feGaussianBlur stdDeviation="2"/>
                                  <feComposite in2="hardAlpha" operator="out"/>
                                  <feColorMatrix type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.25 0"/>
                                  <feBlend mode="normal" in2="BackgroundImageFix" result="effect1_dropShadow_461_4148"/>
                                  <feBlend mode="normal" in="SourceGraphic" in2="effect1_dropShadow_461_4148" result="shape"/>
                                  </filter>
                                  </defs>
                              </svg>
                          </div>
                      </div>
                  </div>
                </div>
                <div className="slider-text-wrapper">
                  <div className="slider-text">7시간 30분</div>
                  {/* <div className="slider-text-subtext">총 투자 시간</div> */}
                </div>
              </div>

              <div className="form-section">
                <label className="form-label">Github 연결*</label>
                <div className="repo-options">
                  <div className={`repo-option-card ${selectedRepoOption === 0 ? 'active' : ''}`} onClick={() => setSelectedRepoOption(0)}>
                    <div className="repo-option-content">
                      <div className="repo-icon-wrapper folder-icon">
                        <FolderIcon />
                      </div>
                      <div className="repo-text-content">
                        <div className={`repo-title ${selectedRepoOption === 0 ? '' : 'repo-title-dark'}`}>현재 레포지토리</div>
                        <div className={`repo-url ${selectedRepoOption === 0 ? '' : 'repo-title-dark'}`}>https://github.com/spellcheck/hanspell.git</div>
                      </div>
                    </div>
                    <div className={`radio-check-wrapper ${selectedRepoOption === 0 ? 'checked' : ''}`}>
                      {selectedRepoOption === 0 && <CheckIcon />}
                    </div>
                  </div>
                  
                  <div className={`repo-option-card ${selectedRepoOption === 1 ? 'active' : ''}`} onClick={() => setSelectedRepoOption(1)}>
                    <div className="repo-option-content">
                      <div className="repo-icon-wrapper add-icon">
                        <AddIcon />
                      </div>
                      <div className="repo-text-content">
                        <div className={`repo-title ${selectedRepoOption === 1 ? '' : 'repo-title-dark'}`}>새로운 레포지토리 만들기</div>
                      </div>
                    </div>
                    <div className={`radio-check-wrapper ${selectedRepoOption === 1 ? 'checked' : ''}`}>
                      {selectedRepoOption === 1 && <CheckIcon />}
                    </div>
                  </div>

                  <div className={`repo-option-card ${selectedRepoOption === 2 ? 'active' : ''}`} onClick={() => setSelectedRepoOption(2)}>
                    <div className="repo-option-content">
                      <div className="repo-icon-wrapper repo-icon">
                        <RepoIcon />
                      </div>
                      <div className="repo-text-content">
                        <div className={`repo-title ${selectedRepoOption === 2 ? '' : 'repo-title-dark'}`}>기존 레포지토리 연결하기</div>
                      </div>
                    </div>
                    <div className={`radio-check-wrapper ${selectedRepoOption === 2 ? 'checked' : ''}`}>
                      {selectedRepoOption === 2 && <CheckIcon />}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="modal-footer">
            <div className="button-group">
              <button className="btn btn-secondary">취소</button>
              <button className="btn btn-primary">확인</button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default ProjectEditModal;
