import { useState } from 'react'
import Title from '@/components/atoms/Title'
import Button from '@/components/atoms/Button'
import RepositoryOptionCard from '@/components/molecules/RepositoryOptionCard'
import RepositoryUrlInput from '@/components/molecules/RepositoryUrlInput'
import styles from './GithubConnection.module.css'

const GithubConnection = () => {
    const [selectedOption, setSelectedOption] = useState(null)      // 전역 관리 필요 X
    const [repositoryUrl, setRepositoryUrl] = useState('')          // 마찬가지

    const handleSelect = (option) => {
        setSelectedOption(option)
    }

    const handleCreate = () => {
        if (selectedOption === 'existing') {
            if (repositoryUrl.trim()) {
                console.log(repositoryUrl);
            } else {
                alert('레포지토리 URL을 입력해주세요.');
            }
        } else if (selectedOption === 'new') {
            console.log('api 요청');
        } else {
            alert('레포지토리 옵션을 선택해주세요.');
        }
    }

    return (
        <div className={styles.githubConnectContainer}>
            <section className={styles.section}>
                <Title className={styles.sectionTitle}>Github 연동</Title>

                <div className={styles.repoOptions}>
                    {/* TODO: RepositoryOptionCard 간격 조정하기 */}
                    <RepositoryOptionCard 
                        text="기존 레포지토리 연결"
                        selected={selectedOption == 'existing'}
                        onClick={() => handleSelect('existing')}
                        className={`${styles.repoCard} ${selectedOption === 'existing' ? styles.selected : ''}`}
                    />
                    <RepositoryUrlInput 
                        show={selectedOption === 'existing'}
                        value={repositoryUrl}
                        onChange={(e) => setRepositoryUrl(e.target.value)}
                        className={`${styles.repoUrlContainer} ${selectedOption === 'existing' ? styles.show : ''}`}
                        inputClassName={styles.repoUrlInput}
                    />


                    <RepositoryOptionCard 
                        text="새로운 레포지토리 생성"
                        selected={selectedOption == 'new'}
                        onClick={() => handleSelect('new')}
                        className={`${styles.repoCard} ${selectedOption === 'new' ? styles.selected : ''}`}
                    />
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