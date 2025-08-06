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
                    <div className={styles.featuresTitle}>
                        <div className={styles.featuresTitleText}>주요 기능</div>
                    </div>
                    <div className={styles.featuresList}>
                        {/* 회원가입 기능 */}
                        <div className={styles.featureCard}>
                            <div className={styles.featureHeader}>
                                <div className={styles.featureTitle}>
                                    <div className={styles.featureName}>회원가입</div>
                                    <div className={`${styles.priorityBadge} ${styles.priorityLow}`}>
                                        <div className={`${styles.priorityText} ${styles.priorityLowText}`}>Low</div>
                                    </div>
                                </div>
                                <div className={styles.featureActions}>
                                    <div className={styles.timeEstimate}>
                                        <div className={styles.clockIcon}>
                                            <div className={styles.clockIconInner1}></div>
                                            <div className={styles.clockIconInner2}></div>
                                        </div>
                                        <div className={styles.timeText}>
                                            <div className={styles.timeValue}>5시간</div>
                                        </div>
                                    </div>
                                    <div className={styles.expandIcon}>
                                        <div className={styles.expandIconInner}></div>
                                    </div>
                                </div>
                            </div>
                            <div className={styles.featureContent}>
                                <div className={styles.featureItems}>
                                    <div className={styles.featureItem}>
                                        <div className={styles.featureItemContent}>
                                            <div className={styles.featureItemName}>
                                                <div className={styles.featureItemText}>일반 회원가입</div>
                                            </div>
                                            <div className={`${styles.priorityBadge} ${styles.priorityNormal}`}>
                                                <div className={`${styles.priorityText} ${styles.priorityNormalText}`}>Normal</div>
                                            </div>
                                        </div>
                                        <div className={styles.featureItemActions}>
                                            <div className={styles.timeEstimate}>
                                                <div className={styles.clockIcon}>
                                                    <div className={styles.clockIconInner1}></div>
                                                    <div className={styles.clockIconInner2}></div>
                                                </div>
                                                <div className={styles.timeText}>
                                                    <div className={styles.timeValue}>2시간</div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div style={{alignSelf: 'stretch', justifyContent: 'flex-start', alignItems: 'center', gap: '10px', display: 'inline-flex'}}>
                                        <div className={styles.featureItem} style={{width: '453px', height: '55px'}}>
                                            <div className={styles.featureItemContent}>
                                                <div className={styles.featureItemName}>
                                                    <div className={styles.featureItemText}>카카오톡 회원가입 연동</div>
                                                </div>
                                                <div className={`${styles.priorityBadge} ${styles.priorityLow}`}>
                                                    <div className={`${styles.priorityText} ${styles.priorityLowText}`}>Low</div>
                                                </div>
                                            </div>
                                            <div className={styles.featureItemActions} style={{width: '57px'}}>
                                                <div className={styles.timeEstimate}>
                                                    <div className={styles.clockIcon}>
                                                        <div className={styles.clockIconInner1}></div>
                                                        <div className={styles.clockIconInner2}></div>
                                                    </div>
                                                    <div className={styles.timeText}>
                                                        <div className={styles.timeValue}>3시간</div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div style={{width: '21px', height: '21px', background: '#D9D9D9'}}></div>
                                    </div>
                                    <div className={styles.addTaskButton} style={{paddingTop: '8px'}}>
                                        <div style={{justifyContent: 'flex-start', alignItems: 'center', gap: '8px', display: 'flex'}}>
                                            <div style={{width: '16px', height: '16px', background: '#D9D9D9'}}></div>
                                            <div className={styles.addTaskText}>
                                                <div className={styles.addTaskTextContent}>새 작업 추가</div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* 데이터 수집 및 전처리 기능 */}
                        <div className={styles.featureCard}>
                            <div className={styles.featureHeader}>
                                <div className={styles.featureTitle}>
                                    <div className={styles.featureName} style={{width: '145px', height: '23px'}}>데이터 수집 및 전처리</div>
                                    <div className={`${styles.priorityBadge} ${styles.priorityHigh}`}>
                                        <div className={`${styles.priorityText} ${styles.priorityHighText}`}>High</div>
                                    </div>
                                </div>
                                <div className={styles.featureActions}>
                                    <div className={styles.timeEstimate}>
                                        <div className={styles.clockIcon}>
                                            <div className={styles.clockIconInner1}></div>
                                            <div className={styles.clockIconInner2}></div>
                                        </div>
                                        <div className={styles.timeText}>
                                            <div className={styles.timeValue}>8시간</div>
                                        </div>
                                    </div>
                                    <div className={styles.expandIcon}>
                                        <div className={styles.expandIconInner}></div>
                                    </div>
                                    <div className={styles.deleteIcon}>
                                        <div className={styles.deleteIconInner}></div>
                                    </div>
                                </div>
                            </div>
                            <div className={styles.featureContent}>
                                <div className={styles.featureItems}>
                                    <div className={styles.expandedContent}>
                                        <div className={styles.expandedHeader}>
                                            <div className={styles.featureItemContent}>
                                                <div className={styles.featureItemName}>
                                                    <div className={styles.featureItemText}>데이터 수집</div>
                                                </div>
                                                <div className={`${styles.priorityBadge} ${styles.priorityHigh}`}>
                                                    <div className={`${styles.priorityText} ${styles.priorityHighText}`}>High</div>
                                                </div>
                                            </div>
                                            <div className={styles.featureItemActions}>
                                                <div className={styles.timeEstimate}>
                                                    <div className={styles.clockIcon}>
                                                        <div className={styles.clockIconInner1}></div>
                                                        <div className={styles.clockIconInner2}></div>
                                                    </div>
                                                    <div className={styles.timeText}>
                                                        <div className={styles.timeValue}>4시간</div>
                                                    </div>
                                                </div>
                                                <div className={styles.expandIcon}>
                                                    <div className={styles.expandIconInner}></div>
                                                </div>
                                                <div className={styles.deleteIcon}>
                                                    <div className={styles.deleteIconInner}></div>
                                                </div>
                                            </div>
                                        </div>
                                        <div className={styles.expandedBody}>
                                            <div className={styles.expandedItems}>
                                                <div className={styles.expandedItem}>
                                                    <div className={styles.expandedItemContent}>
                                                        <div className={styles.featureItemContent}>
                                                            <div className={styles.featureItemName}>
                                                                <div className={styles.featureItemText}>웹페이지 크롤링</div>
                                                            </div>
                                                            <div className={`${styles.priorityBadge} ${styles.priorityNormal}`}>
                                                                <div className={`${styles.priorityText} ${styles.priorityNormalText}`}>Normal</div>
                                                            </div>
                                                        </div>
                                                        <div className={styles.expandedItemActions}>
                                                            <div className={styles.timeEstimate}>
                                                                <div className={styles.clockIcon}>
                                                                    <div className={styles.clockIconInner1}></div>
                                                                    <div className={styles.clockIconInner2}></div>
                                                                </div>
                                                                <div className={styles.timeText}>
                                                                    <div className={styles.timeValue}>2시간</div>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <div className={styles.deleteIcon}>
                                                        <div className={`${styles.deleteIconInner} ${styles.deleteIconRed}`}></div>
                                                    </div>
                                                </div>
                                                <div className={styles.featureItem}>
                                                    <div className={styles.featureItemContent}>
                                                        <div className={styles.featureItemName}>
                                                            <div className={styles.featureItemText}>API</div>
                                                        </div>
                                                        <div className={`${styles.priorityBadge} ${styles.priorityNormal}`}>
                                                            <div className={`${styles.priorityText} ${styles.priorityNormalText}`}>Normal</div>
                                                        </div>
                                                    </div>
                                                    <div className={styles.featureItemActions}>
                                                        <div className={styles.timeEstimate}>
                                                            <div className={styles.clockIcon}>
                                                                <div className={styles.clockIconInner1}></div>
                                                                <div className={styles.clockIconInner2}></div>
                                                            </div>
                                                            <div className={styles.timeText}>
                                                                <div className={styles.timeValue}>2시간</div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                                <div className={styles.addTaskButton} style={{paddingTop: '8px'}}>
                                                    <div style={{justifyContent: 'flex-start', alignItems: 'center', gap: '8px', display: 'flex'}}>
                                                        <div style={{width: '16px', height: '16px', background: '#D9D9D9'}}></div>
                                                        <div className={styles.addTaskText}>
                                                            <div className={styles.addTaskTextContent}>새 작업 추가</div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div className={styles.collapsedItem}>
                                        <div className={styles.collapsedItemInner}>
                                            <div className={styles.featureItemContent}>
                                                <div className={styles.featureItemName}>
                                                    <div className={styles.featureItemText}>데이터 전처리</div>
                                                </div>
                                                <div className={`${styles.priorityBadge} ${styles.priorityHigh}`}>
                                                    <div className={`${styles.priorityText} ${styles.priorityHighText}`}>High</div>
                                                </div>
                                            </div>
                                            <div className={styles.collapsedItemActions}>
                                                <div className={styles.timeEstimate}>
                                                    <div className={styles.clockIcon}>
                                                        <div className={styles.clockIconInner1}></div>
                                                        <div className={styles.clockIconInner2}></div>
                                                    </div>
                                                    <div className={styles.timeText}>
                                                        <div className={styles.timeValue}>4시간</div>
                                                    </div>
                                                </div>
                                                <div className={styles.collapseIcon}>
                                                    <div className={styles.collapseIconInner}></div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div className={styles.addTaskButton} style={{paddingTop: '8px'}}>
                                        <div style={{justifyContent: 'flex-start', alignItems: 'center', gap: '9px', display: 'flex'}}>
                                            <div style={{width: '17px', height: '16px', background: '#D9D9D9'}}></div>
                                            <div className={styles.addTaskText}>
                                                <div className={styles.addTaskTextContent}>새 작업 추가</div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* 배포 기능 */}
                        <div className={styles.simpleCard}>
                            <div className={styles.simpleCardContent}>
                                <div className={styles.simpleCardTitle}>배포</div>
                                <div className={`${styles.priorityBadge} ${styles.priorityNormal}`}>
                                    <div className={`${styles.priorityText} ${styles.priorityNormalText}`}>Normal</div>
                                </div>
                            </div>
                            <div className={styles.simpleCardActions}>
                                <div className={styles.timeEstimate}>
                                    <div className={styles.clockIcon}>
                                        <div className={styles.clockIconInner1}></div>
                                        <div className={styles.clockIconInner2}></div>
                                    </div>
                                    <div className={styles.timeText}>
                                        <div className={styles.timeValue}>5시간</div>
                                    </div>
                                </div>
                                <div className={styles.collapseIcon}>
                                    <div className={styles.collapseIconInner}></div>
                                </div>
                            </div>
                        </div>
                        <div className={styles.addTaskButton} style={{paddingTop: '8px', boxShadow: '0px 3px 3px rgba(233, 236, 239, 0.25)'}}>
                            <div style={{justifyContent: 'flex-start', alignItems: 'center', gap: '8px', display: 'flex'}}>
                                <div style={{width: '16px', height: '16px', background: '#D9D9D9'}}></div>
                                <div className={styles.addTaskText}>
                                    <div className={styles.addTaskTextContent}>새 작업 추가</div>
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