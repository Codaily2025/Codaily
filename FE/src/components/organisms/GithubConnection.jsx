import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Title from '@/components/atoms/Title'
import Button from '@/components/atoms/Button'
import RepositoryOptionCard from '@/components/molecules/RepositoryOptionCard'
import RepositoryUrlInput from '@/components/molecules/RepositoryUrlInput'
import styles from './GithubConnection.module.css'
import { useLinkGithubRepoMutation, useCreateGithubRepoMutation } from '@/queries/useGitHub'

const GithubConnection = () => {
    const [selectedOption, setSelectedOption] = useState(null)                      // 전역 관리 필요 X
    const [existedRepositoryUrl, setExistedRepositoryUrl] = useState('')            // 마찬가지
    const [newRepositoryUrl, setNewRepositoryUrl] = useState('')                    // 마찬가지
    
    const navigate = useNavigate()
    
    // github 연동 후 해당 프로젝트 페이지로 네비게이션
    const handleNavigateToProject = (projectId) => {
        navigate(`/project/${projectId}`)
    }
    
    // github 저장소 연동/생성 mutation 훅 생성
    // - handleNavigateToProject: mutation 성공 시 실행할 콜백 함수
    // - mutate 실행 시 넘긴 variables.projectId를 콜백 인자로 전달
    const linkGithubRepoMutation = useLinkGithubRepoMutation(handleNavigateToProject)
    const createGithubRepoMutation = useCreateGithubRepoMutation(handleNavigateToProject)

    const handleSelect = (option) => {
        setSelectedOption(option)
    }

    // projectId 테스트용 하드코딩
    // TODO: 프로젝트 생성 페이지 내에서 projectId 어떻게 관리되는지 확인 후 수정
    const projectId = 10

    const handleCreate = () => {
        if (selectedOption === 'existing') {
            if (existedRepositoryUrl.trim()) {
                console.log(existedRepositoryUrl)
                // const repoName = existedRepositoryUrl.split('/').pop().replace('.git', '')
                const repoName = existedRepositoryUrl
                linkGithubRepoMutation.mutate({ projectId, repoName })
            } else {
                alert('레포지토리 URL을 입력해주세요.')
            }
        } else if (selectedOption === 'new') {
            if (newRepositoryUrl.trim()) {
                console.log(newRepositoryUrl)
                // const repoName = newRepositoryUrl.split('/').pop().replace('.git', '')
                const repoName = newRepositoryUrl
                createGithubRepoMutation.mutate({ projectId, repoName })
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
