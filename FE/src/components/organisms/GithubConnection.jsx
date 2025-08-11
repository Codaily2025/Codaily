import { useState } from 'react'
import Title from '@/components/atoms/Title'
import Button from '@/components/atoms/Button'
import RepositoryOptionCard from '@/components/molecules/RepositoryOptionCard'
import RepositoryUrlInput from '@/components/molecules/RepositoryUrlInput'
import styles from './GithubConnection.module.css'

const GithubConnection = () => {
    const [selectedOption, setSelectedOption] = useState(null)      // 전역 관리 필요 X
    const [existedRepositoryUrl, setExistedRepositoryUrl] = useState('')          // 마찬가지
    const [newRepositoryUrl, setNewRepositoryUrl] = useState('')          // 마찬가지

    const handleSelect = (option) => {
        setSelectedOption(option)
    }

    const handleCreate = () => {
        if (selectedOption === 'existing') {
            if (existedRepositoryUrl.trim()) {
                console.log(existedRepositoryUrl)
            } else {
                alert('레포지토리 URL을 입력해주세요.')
            }
        } else if (selectedOption === 'new') {
            if (newRepositoryUrl.trim()) {
                console.log(newRepositoryUrl)
            } else {
                alert('레포지토리 URL을 입력해주세요.')
            }
        } else {
            alert('레포지토리 옵션을 선택해주세요.')
        }
    }

    return (
        <div className={styles.githubConnectContainer}>
            <section className={styles.section}>
                <Title className={styles.sectionTitle}>Github 연동</Title>

                <div className={styles.repoOptions}>
                    <RepositoryOptionCard 
                        text="기존 레포지토리 연결"
                        selected={selectedOption == 'existing'}
                        onClick={() => handleSelect('existing')}
                        className={`${styles.repoCard} ${selectedOption === 'existing' ? styles.selected : ''}`}
                    />
                    
                    {/* 조건부 렌더링: 'existing' 선택 시에만 렌더링 */}
                    {selectedOption === 'existing' && (
                        <RepositoryUrlInput 
                            show={true}
                            value={existedRepositoryUrl}
                            onChange={(e) => setExistedRepositoryUrl(e.target.value)}
                            className={styles.repoUrlContainer}
                            inputClassName={styles.repoUrlInput}
                        />
                    )}

                    <RepositoryOptionCard 
                        text="새로운 레포지토리 생성"
                        selected={selectedOption == 'new'}
                        onClick={() => handleSelect('new')}
                        className={`${styles.repoCard} ${selectedOption === 'new' ? styles.selected : ''}`}
                    />

                    {/* 조건부 렌더링: 'new' 선택 시에만 렌더링 */}
                    {selectedOption === 'new' && (
                        <RepositoryUrlInput 
                            show={true}
                            value={newRepositoryUrl}
                            onChange={(e) => setNewRepositoryUrl(e.target.value)}
                            className={styles.repoUrlContainer}
                            inputClassName={styles.repoUrlInput}
                        />
                    )}

                </div>

                <div className={styles.actionButtons}>
                    <Button type='secondary' onClick={() => console.log('이전 섹션으로')}>이전으로</Button>
                    <Button type='primary' onClick={handleCreate}>생성하기</Button>
                </div>
            </section>
        </div>
    )
}

export default GithubConnection
