import React from 'react';
import styles from './RequirementsSpecification.module.css';

const RequirementsSpecification = () => {
    return (
        <div className={styles.requirementsSidebar}>
            <div className={styles.container}>
                {/* 헤더 */}
                <div className={styles.header}>
                    <div className={styles.headerContent}>
                        <div className={styles.title}>요구사항 명세서</div>
                    </div>
                    <div className={styles.pdfIcon}>
                        <div className={styles.pdfIconInner1}></div>
                        <div className={styles.pdfIconInner2}></div>
                    </div>
                    <div className={styles.pdfText}>PDF 내보내기</div>
                </div>

                {/* 예상 작업 완료일 */}
                <div className={styles.card}>
                    <div className={styles.cardHeader}>
                        <div className={styles.cardTitle}>📈 예상 작업 완료일 : 2025.08.23</div>
                    </div>
                </div>

                {/* 프로젝트 개요 */}
                <div className={styles.card}>
                    <div className={styles.cardHeader}>
                        <div className={styles.cardTitle}>프로젝트 개요</div>
                    </div>
                    <div className={styles.projectOverview}>
                        <div className={styles.overviewItem}>
                            <div className={styles.bullet}>•</div>
                            <div className={styles.itemContent}>
                                <span className={styles.itemLabel}>프로젝트명: </span>
                                <span className={styles.itemValue}>RAG 요리 레시피 챗봇</span>
                            </div>
                        </div>
                        <div className={styles.overviewItem}>
                            <div className={styles.bullet}>•</div>
                            <div className={styles.itemContent}>
                                <span className={styles.itemLabel}>목적:</span>
                                <span className={styles.itemValue}> 가정에서 쉽게 요리하고 싶은 사람들을 위한 AI 요리 도우미</span>
                            </div>
                        </div>
                        <div className={styles.descriptionContainer}>
                            <div className={styles.descriptionHeader}>
                                <div className={styles.bullet}>•</div>
                                <div className={styles.itemLabel}>설명</div>
                            </div>
                            <div className={styles.descriptionText}>
                                식품영양DB API를 활용한 사용자 맞춤형 메뉴 추천 플랫폼입니다. 사용자가 레시피를 요청하면 해당 요리의 레시피 정보를 알려줍니다. 사용자가 영양 정보를 요청하면 특정 요리의 영양 정보를 알려줍니다. 사용자가 사용하기를 원하는 재료를 입력하면 재료들을 활용할 수 있는 레시피를 알려줍니다. RAG 파이프라인을 사용해 AI 기반의 응답을 생성하여 사용자에게 모바일 웹 화면으로 보여줍니다.
                            </div>
                        </div>
                    </div>
                </div>

                {/* 기술 스택 */}
                <div className={styles.card}>
                    <div className={styles.techStackHeader}>
                        <div className={styles.techStackTitle}>
                            <div className={styles.cardTitle}>기술 스택</div>
                        </div>
                        <div className={styles.addTechButton}>
                            <div className={styles.addTechText}>기술 추가하기</div>
                        </div>
                    </div>
                    <div className={styles.techTags}>
                        <div className={styles.techTagRow}>
                            <div className={styles.techTag}>
                                <div className={styles.techTagText}>Python</div>
                            </div>
                            <div className={styles.techTag}>
                                <div className={styles.techTagText}>FastAPI</div>
                            </div>
                            <div className={styles.techTag}>
                                <div className={styles.techTagText}>RAG Pipeline</div>
                            </div>
                            <div className={styles.techTag}>
                                <div className={styles.techTagText}>Vector DB</div>
                            </div>
                            <div className={styles.techTag}>
                                <div className={styles.techTagText}>AWS EC2</div>
                            </div>
                            <div className={styles.techTag}>
                                <div className={styles.techTagText}>AWS RDS</div>
                            </div>
                        </div>
                        <div className={styles.techTagRow}>
                            <div className={styles.techTag}>
                                <div className={styles.techTagText}>AWS S3</div>
                            </div>
                            <div className={styles.techTag}>
                                <div className={styles.techTagText}>AWS S3</div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* 주요 기능 */}
                <div className={styles.card}>
                {/* <div className={styles.mainFeaturesCard}> */}
                    <div className={styles.mainFeaturesHeader}>
                        <div className={styles.mainFeaturesTitle}>주요 기능</div>
                    </div>
                    <div className={styles.mainFeaturesList}>
                        {/* 회원가입 기능 */}
                        <div className={styles.mainFeatureCard}>
                            <div className={styles.mainFeatureHeader}>
                                <div className={styles.mainFeatureHeaderLeft}>
                                    <div className={styles.checkbox}>
                                        <div className={styles.checkboxIcon}>
                                            <svg width="14" height="15" viewBox="0 0 14 15" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M11.6693 4L5.2526 10.4167L2.33594 7.5" stroke="#5A597D" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                            </svg>
                                        </div>
                                    </div>
                                    <div className={styles.mainFeatureName}>회원가입</div>
                                    <div className={styles.priorityBadgeLow}>
                                        <div className={styles.priorityTextLow}>Low</div>
                                    </div>
                                </div>
                                <div className={styles.mainFeatureHeaderRight}>
                                    <div className={styles.timeIndicator}>
                                        <div className={styles.timeIconContainer}>
                                            <svg width="16" height="17" viewBox="0 0 16 17" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M8.0026 15.1666C11.6845 15.1666 14.6693 12.1818 14.6693 8.49992C14.6693 4.81802 11.6845 1.83325 8.0026 1.83325C4.32071 1.83325 1.33594 4.81802 1.33594 8.49992C1.33594 12.1818 4.32071 15.1666 8.0026 15.1666Z" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                            <path d="M8 4.5V8.5L10.6667 9.83333" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                            </svg>
                                        </div>
                                        <div className={styles.timeTextContainer}>
                                            <div className={styles.timeValueText}>5시간</div>
                                        </div>
                                    </div>
                                    <div className={styles.expandIconContainer}>
                                        <svg width="20" height="21" viewBox="0 0 20 21" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M15 13L10 8L5 13" stroke="#6C757D" strokeWidth="2.08333" strokeLinecap="round" strokeLinejoin="round"/>
                                        </svg>
                                    </div>
                                </div>
                            </div>
                            <div className={styles.mainFeatureContent}>
                                <div className={styles.mainFeatureItems}>
                                    <div className={styles.subTaskItem}>
                                        <div className={styles.subTaskLeft}>
                                            <div className={styles.checkbox}>
                                                <div className={styles.checkboxIcon}>
                                                    <svg width="14" height="15" viewBox="0 0 14 15" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <path d="M11.6693 4L5.2526 10.4167L2.33594 7.5" stroke="#5A597D" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                    </svg>
                                                </div>
                                            </div>
                                            <div className={styles.subTaskNameContainer}>
                                                <div className={styles.subTaskName}>일반 회원가입</div>
                                            </div>
                                            <div className={styles.priorityBadgeNormal}>
                                                <div className={styles.priorityTextNormal}>Normal</div>
                                            </div>
                                        </div>
                                        <div className={styles.subTaskRight}>
                                            <div className={styles.subTaskActions}>
                                                <div className={styles.timeIconContainer}>
                                                    <svg width="16" height="17" viewBox="0 0 16 17" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <path d="M8.0026 15.1666C11.6845 15.1666 14.6693 12.1818 14.6693 8.49992C14.6693 4.81802 11.6845 1.83325 8.0026 1.83325C4.32071 1.83325 1.33594 4.81802 1.33594 8.49992C1.33594 12.1818 4.32071 15.1666 8.0026 15.1666Z" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                                    <path d="M8 4.5V8.5L10.6667 9.83333" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                                    </svg>
                                                </div>
                                                <div className={styles.timeTextContainer}>
                                                    <div className={styles.timeValueText}>2시간</div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div className={styles.subTaskItem}>
                                        <div className={styles.subTaskLeft}>
                                            <div className={styles.checkbox}>
                                                <div className={styles.checkboxIcon}>
                                                    <svg width="14" height="15" viewBox="0 0 14 15" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <path d="M11.6693 4L5.2526 10.4167L2.33594 7.5" stroke="#5A597D" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                    </svg>
                                                </div>
                                            </div>
                                            <div className={styles.subTaskNameContainer}>
                                                <div className={styles.subTaskName}>카카오톡 회원가입 연동</div>
                                            </div>
                                            <div className={styles.priorityBadgeLow}>
                                                <div className={styles.priorityTextLow}>Low</div>
                                            </div>
                                        </div>
                                        <div className={styles.subTaskTime}>
                                            <div className={styles.subTaskActions}>
                                                <div className={styles.timeIconContainer}>
                                                    <svg width="16" height="17" viewBox="0 0 16 17" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <path d="M8.0026 15.1666C11.6845 15.1666 14.6693 12.1818 14.6693 8.49992C14.6693 4.81802 11.6845 1.83325 8.0026 1.83325C4.32071 1.83325 1.33594 4.81802 1.33594 8.49992C1.33594 12.1818 4.32071 15.1666 8.0026 15.1666Z" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                                    <path d="M8 4.5V8.5L10.6667 9.83333" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                                    </svg>
                                                </div>
                                                <div className={styles.timeTextContainer}>
                                                    <div className={styles.timeValueText}>3시간</div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div className={styles.addNewTaskSection}>
                                        <div className={styles.addNewTaskButton}>
                                            <div className={styles.addIconContainer}>
                                                <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M8 3.33325V12.6666" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                                <path d="M3.33594 8H12.6693" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                                </svg>
                                            </div>
                                            <div className={styles.addNewTaskText}>
                                                <div className={styles.addNewTaskTextContent}>새 작업 추가</div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        {/* 데이터 수집 및 전처리 기능 */}
                        <div className={styles.mainFeatureCard}>
                            <div className={styles.mainFeatureHeader}>
                                <div className={styles.mainFeatureHeaderLeft}>
                                    <div className={styles.checkbox}>
                                        <div className={styles.checkboxIcon}>
                                            <svg width="14" height="15" viewBox="0 0 14 15" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M11.6693 4L5.2526 10.4167L2.33594 7.5" stroke="#5A597D" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                            </svg>
                                        </div>
                                    </div>
                                    <div className={styles.mainFeatureDataName}>데이터 수집 및 전처리</div>
                                    <div className={styles.priorityBadgeHigh}>
                                        <div className={styles.priorityTextHigh}>High</div>
                                    </div>
                                </div>
                                <div className={styles.mainFeatureHeaderRight}>
                                    <div className={styles.timeIndicator}>
                                        <div className={styles.timeIconContainer}>
                                            <svg width="16" height="17" viewBox="0 0 16 17" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M8.0026 15.1666C11.6845 15.1666 14.6693 12.1818 14.6693 8.49992C14.6693 4.81802 11.6845 1.83325 8.0026 1.83325C4.32071 1.83325 1.33594 4.81802 1.33594 8.49992C1.33594 12.1818 4.32071 15.1666 8.0026 15.1666Z" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                            <path d="M8 4.5V8.5L10.6667 9.83333" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                            </svg>
                                        </div>
                                        <div className={styles.timeTextContainer}>
                                            <div className={styles.timeValueText}>8시간</div>
                                        </div>
                                    </div>
                                    <div className={styles.expandIconContainer}>
                                        <svg width="20" height="21" viewBox="0 0 20 21" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M15 13L10 8L5 13" stroke="#6C757D" strokeWidth="2.08333" strokeLinecap="round" strokeLinejoin="round"/>
                                        </svg>
                                    </div>
                                </div>
                            </div>
                            <div className={styles.mainFeatureContent}>
                                <div className={styles.mainFeatureItems}>
                                    <div className={styles.expandedSection}>
                                        <div className={styles.expandedSectionHeader}>
                                            <div className={styles.expandedSectionHeaderInner}>
                                                <div className={styles.subTaskLeft}>
                                                    <div className={styles.checkbox}>
                                                        <div className={styles.checkboxIcon}>
                                                            <svg width="14" height="15" viewBox="0 0 14 15" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                            <path d="M11.6693 4L5.2526 10.4167L2.33594 7.5" stroke="#5A597D" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                            </svg>
                                                        </div>
                                                    </div>
                                                    <div className={styles.subTaskNameContainer}>
                                                        <div className={styles.subTaskName}>데이터 수집</div>
                                                    </div>
                                                    <div className={styles.priorityBadgeHigh}>
                                                        <div className={styles.priorityTextHigh}>High</div>
                                                    </div>
                                                </div>
                                                <div className={styles.mainFeatureHeaderRight}>
                                                    <div className={styles.timeIndicator}>
                                                        <div className={styles.timeIconContainer}>
                                                            <svg width="16" height="17" viewBox="0 0 16 17" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                            <path d="M8.0026 15.1666C11.6845 15.1666 14.6693 12.1818 14.6693 8.49992C14.6693 4.81802 11.6845 1.83325 8.0026 1.83325C4.32071 1.83325 1.33594 4.81802 1.33594 8.49992C1.33594 12.1818 4.32071 15.1666 8.0026 15.1666Z" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                                            <path d="M8 4.5V8.5L10.6667 9.83333" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                                            </svg>
                                                        </div>
                                                        <div className={styles.timeTextContainer}>
                                                            <div className={styles.timeValueText}>4시간</div>
                                                        </div>
                                                    </div>
                                                    <div className={styles.expandIconContainer}>
                                                        <svg width="20" height="21" viewBox="0 0 20 21" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                        <path d="M15 13L10 8L5 13" stroke="#6C757D" strokeWidth="2.08333" strokeLinecap="round" strokeLinejoin="round"/>
                                                        </svg>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div className={styles.expandedSectionBody}>
                                            <div className={styles.expandedSectionItems}>
                                                <div className={styles.expandedSectionItem}>
                                                    <div className={styles.subTaskLeft}>
                                                        <div className={styles.checkbox}>
                                                            <div className={styles.checkboxIcon}>
                                                                <svg width="14" height="15" viewBox="0 0 14 15" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                                <path d="M11.6693 4L5.2526 10.4167L2.33594 7.5" stroke="#5A597D" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                                </svg>
                                                            </div>
                                                        </div>
                                                        <div className={styles.subTaskNameContainer}>
                                                            <div className={styles.subTaskName}>웹페이지 크롤링</div>
                                                        </div>
                                                        <div className={styles.priorityBadgeNormal}>
                                                            <div className={styles.priorityTextNormal}>Normal</div>
                                                        </div>
                                                    </div>
                                                    <div className={styles.subTaskRight}>
                                                        <div className={styles.subTaskActions}>
                                                            <div className={styles.timeIconContainer}>
                                                                <svg width="16" height="17" viewBox="0 0 16 17" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                                <path d="M8.0026 15.1666C11.6845 15.1666 14.6693 12.1818 14.6693 8.49992C14.6693 4.81802 11.6845 1.83325 8.0026 1.83325C4.32071 1.83325 1.33594 4.81802 1.33594 8.49992C1.33594 12.1818 4.32071 15.1666 8.0026 15.1666Z" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                                                <path d="M8 4.5V8.5L10.6667 9.83333" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                                                </svg>
                                                            </div>
                                                            <div className={styles.timeTextContainer}>
                                                                <div className={styles.timeValueText}>2시간</div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                                <div className={styles.expandedSectionItem}>
                                                    <div className={styles.subTaskLeft}>
                                                        <div className={styles.checkbox}>
                                                            <div className={styles.checkboxIcon}>
                                                                <svg width="14" height="15" viewBox="0 0 14 15" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                                <path d="M11.6693 4L5.2526 10.4167L2.33594 7.5" stroke="#5A597D" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                                </svg>
                                                            </div>
                                                        </div>
                                                        <div className={styles.subTaskNameContainer}>
                                                            <div className={styles.subTaskName}>API</div>
                                                        </div>
                                                        <div className={styles.priorityBadgeNormal}>
                                                            <div className={styles.priorityTextNormal}>Normal</div>
                                                        </div>
                                                    </div>
                                                    <div className={styles.subTaskRight}>
                                                        <div className={styles.subTaskActions}>
                                                            <div className={styles.timeIconContainer}>
                                                                <svg width="16" height="17" viewBox="0 0 16 17" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                                <path d="M8.0026 15.1666C11.6845 15.1666 14.6693 12.1818 14.6693 8.49992C14.6693 4.81802 11.6845 1.83325 8.0026 1.83325C4.32071 1.83325 1.33594 4.81802 1.33594 8.49992C1.33594 12.1818 4.32071 15.1666 8.0026 15.1666Z" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                                                <path d="M8 4.5V8.5L10.6667 9.83333" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                                                </svg>
                                                            </div>
                                                            <div className={styles.timeTextContainer}>
                                                                <div className={styles.timeValueText}>2시간</div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                                <div className={styles.addNewTaskSection}>
                                                    <div className={styles.addNewTaskButton}>
                                                        <div className={styles.addIconContainer}>
                                                            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                            <path d="M8 3.33325V12.6666" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                                            <path d="M3.33594 8H12.6693" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                                            </svg>
                                                        </div>
                                                        <div className={styles.addNewTaskText}>
                                                            <div className={styles.addNewTaskTextContent}>새 작업 추가</div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div className={styles.simpleToggleItem}>
                                        <div className={styles.simpleToggleInner}>
                                            <div className={styles.subTaskLeft}>
                                                <div className={styles.checkbox}>
                                                    <div className={styles.checkboxIcon}>
                                                        <svg width="14" height="15" viewBox="0 0 14 15" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                        <path d="M11.6693 4L5.2526 10.4167L2.33594 7.5" stroke="#5A597D" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                        </svg>
                                                    </div>
                                                </div>
                                                <div className={styles.subTaskNameContainer}>
                                                    <div className={styles.subTaskName}>데이터 전처리</div>
                                                </div>
                                                <div className={styles.priorityBadgeHigh}>
                                                    <div className={styles.priorityTextHigh}>High</div>
                                                </div>
                                            </div>
                                            <div className={styles.simpleToggleActions}>
                                                <div className={styles.timeIndicator}>
                                                    <div className={styles.timeIconContainer}>
                                                        <svg width="16" height="17" viewBox="0 0 16 17" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                        <path d="M8.0026 15.1666C11.6845 15.1666 14.6693 12.1818 14.6693 8.49992C14.6693 4.81802 11.6845 1.83325 8.0026 1.83325C4.32071 1.83325 1.33594 4.81802 1.33594 8.49992C1.33594 12.1818 4.32071 15.1666 8.0026 15.1666Z" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                                        <path d="M8 4.5V8.5L10.6667 9.83333" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                                        </svg>
                                                    </div>
                                                    <div className={styles.timeTextContainer}>
                                                        <div className={styles.timeValueText}>4시간</div>
                                                    </div>
                                                </div>
                                                <div className={styles.iconContainer}>
                                                    <svg width="24" height="25" viewBox="0 0 24 25" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <path d="M12 15.9L6 9.9L7.4 8.5L12 13.1L16.6 8.5L18 9.9L12 15.9Z" fill="#6C757D"/>
                                                    </svg>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div className={styles.addNewTaskSection}>
                                        <div className={styles.addNewTaskButton}>
                                            <div className={styles.addIconContainer}>
                                                <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M8 3.3335V12.6668" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                                <path d="M3.33594 8H12.6693" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                                </svg>
                                            </div>
                                            <div className={styles.addNewTaskText}>
                                                <div className={styles.addNewTaskTextContent}>새 작업 추가</div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        {/* 배포 기능 */}
                        <div className={styles.simpleFeatureCard}>
                            <div className={styles.simpleFeatureLeft}>
                                <div className={styles.checkbox}>
                                    <div className={styles.checkboxIcon}>
                                        <svg width="14" height="15" viewBox="0 0 14 15" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M11.6693 4L5.2526 10.4167L2.33594 7.5" stroke="#5A597D" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                        </svg>
                                    </div>
                                </div>
                                <div className={styles.simpleFeatureTitle}>배포</div>
                                <div className={styles.priorityBadgeNormal}>
                                    <div className={styles.priorityTextNormal}>Normal</div>
                                </div>
                            </div>
                            <div className={styles.simpleFeatureRight}>
                                <div className={styles.timeIndicator}>
                                    <div className={styles.timeIconContainer}>
                                        <svg width="16" height="17" viewBox="0 0 16 17" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M8.0026 15.1668C11.6845 15.1668 14.6693 12.1821 14.6693 8.50016C14.6693 4.81826 11.6845 1.8335 8.0026 1.8335C4.32071 1.8335 1.33594 4.81826 1.33594 8.50016C1.33594 12.1821 4.32071 15.1668 8.0026 15.1668Z" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                        <path d="M8 4.5V8.5L10.6667 9.83333" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                        </svg>
                                    </div>
                                    <div className={styles.timeTextContainer}>
                                        <div className={styles.timeValueText}>5시간</div>
                                    </div>
                                </div>
                                <div className={styles.iconContainer}>
                                    <svg width="24" height="25" viewBox="0 0 24 25" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M12 15.9L6 9.9L7.4 8.5L12 13.1L16.6 8.5L18 9.9L12 15.9Z" fill="#6C757D"/>
                                    </svg>
                                </div>
                            </div>
                        </div>
                        
                        <div className={styles.addNewTaskSection}>
                            <div className={styles.addNewTaskButton}>
                                <div className={styles.addIconContainer}>
                                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M8 3.3335V12.6668" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                    <path d="M3.33594 8H12.6693" stroke="#6C757D" strokeWidth="1.33333" strokeLinecap="round" strokeLinejoin="round"/>
                                    </svg>
                                </div>
                                <div className={styles.addNewTaskText}>
                                    <div className={styles.addNewTaskTextContent}>새 작업 추가</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default RequirementsSpecification;