import React from 'react';
import './ProjectCreateStep2.css';
import chatbotAvatar from '../../assets/chatbot_avartar.png';
import chatuserAvatar from '../../assets/chatuser_avartar.png';

const ProjectCreateStep2 = () => {
  return (
    <div className="chat-page-container">
      {/* Progress Bar */}
      <div className="progress-bar-container">
        <div className="progress-step completed">
          <div className="progress-icon-wrapper">
            <div className="progress-line" />
            <div className="progress-icon">✓</div>
          </div>
          <div className="progress-label-wrapper">
            <div className="progress-label">일정 생성</div>
          </div>
        </div>
        <div className="progress-step active">
          <div className="progress-icon-wrapper">
            <div className="progress-line inactive" />
            <div className="progress-icon">2</div>
          </div>
          <div className="progress-label-wrapper">
            <div className="progress-label active-label">요구사항 명세서</div>
          </div>
        </div>
        <div className="progress-step">
          <div className="progress-icon-wrapper">
            <div className="progress-icon inactive">3</div>
          </div>
          <div className="progress-label-wrapper">
            <div className="progress-label">GitHub 연동</div>
          </div>
        </div>
      </div>

      {/* Chat Area */}
      <div className="chat-main-content">
        <div className="chat-window-container">
          <div className="chat-window">
            <div className="chat-scroll-area">
              {/* Message 1 (Bot) */}
              <div className="message-row bot">
                <div className="avatar">
                  <img src={chatbotAvatar} alt="Bot Avatar" />
                </div>
                <div className="message-bubble bot-bubble">
                  <div className="message-text">안녕하세요! 프로젝트 관리 도우미 Codaily 입니다.<br />프로젝트 시작할 아이디어를 알려주세요.</div>
                </div>
              </div>

              {/* Message 2 (User) */}
              <div className="message-row user">
                <div className="message-bubble user-bubble">
                  <div className="message-text">요리 레시피를 알려주는 챗봇 만들고 싶어.</div>
                </div>
                <div className="avatar">
                  <img src={chatuserAvatar} alt="User Avatar" />
                </div>
              </div>

              {/* Message 3 (Bot) */}
              <div className="message-row bot">
                <div className="avatar">
                  <img src={chatbotAvatar} alt="Bot Avatar" />
                </div>
                <div className="message-bubble bot-bubble">
                  <div className="message-text">맞춤형 프로젝트 관리를 위해 구체적으로 프로젝트에 대해 설명해주세요.<br />기능 정의서나 유저 플로우 등 참고할 수 있는 파일을 첨부해주셔도 좋아요!</div>
                </div>
              </div>

              {/* Message 4 (User) */}
              <div className="message-row user">
                <div className="message-bubble user-bubble">
                  <div className="message-text">RAG 파이프라인을 기반으로 클라우드 DB를 구축할거야.</div>
                </div>
                <div className="avatar">
                  <img src={chatuserAvatar} alt="User Avatar" />
                </div>
              </div>

              {/* Message 5 (Bot) */}
              <div className="message-row bot">
                <div className="avatar">
                  <img src={chatbotAvatar} alt="Bot Avatar" />
                </div>
                <div className="message-bubble bot-bubble">
                  <div className="message-text">주 사용자는 누구인가요?</div>
                </div>
              </div>

              {/* Message 6 (User) */}
              <div className="message-row user">
                <div className="message-bubble user-bubble">
                  <div className="message-text">주 사용자는 가정에서 쉽게 요리를 하고 싶은 사람들이야.</div>
                </div>
                <div className="avatar">
                  <img src={chatuserAvatar} alt="User Avatar" />
                </div>
              </div>

              {/* Message 7 (Bot) */}
              <div className="message-row bot">
                <div className="avatar">
                  <img src={chatbotAvatar} alt="Bot Avatar" />
                </div>
                <div className="message-bubble bot-bubble">
                  <div className="message-text">어떤 서비스를 제공하실 건가요?</div>
                </div>
              </div>

              {/* Message 8 (User) */}
              <div className="message-row user">
                <div className="message-bubble user-bubble large">
                  <div className="message-text">세 가지 기능을 제공할거야.<br />먼저 사용자가 레시피를 물어보면 레시피 정보를 알려줄거야.<br />그리고 사용자가 영양 정보를 물어보면 영양 정보를 알려줄거야.<br />그리고 사용자가 냉장고에 있는 재료로 어떤 요리를 할 수 있는지 물어보면 레시피를 알려줄 생각이야.</div>
                </div>
                <div className="avatar">
                  <img src={chatuserAvatar} alt="User Avatar" />
                </div>
              </div>

              {/* Message 9 (Bot) */}
              <div className="message-row bot">
                <div className="avatar">
                  <img src={chatbotAvatar} alt="Bot Avatar" />
                </div>
                <div className="message-bubble bot-bubble">
                  <div className="message-text">이제 다음단계로 이동할 수 있어요.<br />아래 버튼을 클릭해 다음 단계로 이동해주세요.<br />또는 추가 질문을 통해 아이디어를 정교화할 수도 있어요.</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Input Bar */}
        <div className="input-bar-container">
          <div className="icon-button file-button">
            <div className="file-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="19" viewBox="0 0 18 19" fill="none">
                <path d="M15.75 11.75V14.75C15.75 15.1478 15.592 15.5294 15.3107 15.8107C15.0294 16.092 14.6478 16.25 14.25 16.25H3.75C3.35218 16.25 2.97064 16.092 2.68934 15.8107C2.40804 15.5294 2.25 15.1478 2.25 14.75V11.75" stroke="#6C6B93" strokeWidth="1.5" />
                <path d="M12.75 6.49976L9 2.74976L5.25 6.49976" stroke="#6C6B93" strokeWidth="1.5" />
                <path d="M9 2.74976V11.7498" stroke="#6C6B93" strokeWidth="1.5" />
              </svg>
              {/* <div className="file-icon-bottom"></div> */}
              {/* <div className="file-icon-top"></div> */}
            </div>
          </div>
          <div className="text-input-wrapper">
            <div className="placeholder-text">추가 정보나 요구사항을 입력해주세요...</div>
          </div>
          <div className="icon-button send-button">
            <div className="send-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="19" height="20" viewBox="0 0 19 20" fill="none">
                <path d="M17.5 2.00024L9.25 10.2502" stroke="white" strokeWidth="1.5" />
                <path d="M17.5 2.00024L12.25 17.0002L9.25 10.2502L2.5 7.25024L17.5 2.00024Z" stroke="white" strokeWidth="1.5" />
              </svg>
              {/* <div className="send-icon-arrow-head"></div> */}
              {/* <div className="send-icon-body"></div> */}
            </div>
          </div>
        </div>
      </div>

      {/* Navigation Buttons */}
      <div className="nav-buttons-container">
        <button className="nav-button prev-button">이전으로</button>
        <button className="nav-button next-button">다음으로</button>
      </div>
    </div>
  );
};

export default ProjectCreateStep2;
