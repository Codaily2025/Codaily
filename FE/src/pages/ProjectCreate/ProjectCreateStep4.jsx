import GithubConnection from '@/components/organisms/GithubConnection'
import ProjectScheduleSetter from '@/components/organisms/ProjectScheduleSetter'

const ProjectCreateStep4 = () => {
    return (
        // 임시 최상단 태그 설정
        // 영은아 지금 디자인이 중요하냐? 일단 만들어라~
        <div style={{ width: '100%', display: 'flex', justifyContent: 'center', gap: '20px' }}>
            <ProjectScheduleSetter />
            <GithubConnection />
        </div>

        

    )

}

export default ProjectCreateStep4