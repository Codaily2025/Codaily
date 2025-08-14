import GithubConnection from '@/components/organisms/GithubConnection'
import ChatProgressBar from '@/components/ChatProgressBar/ChatProgressBar'
import '@/pages/ProjectCreate/ProjectCreate.css'

const ProjectCreateStep4 = () => {
    return (
        <div className="project-create-container">
            <ChatProgressBar currentStep={2} />
            <GithubConnection />
        </div>

        

    )

}

export default ProjectCreateStep4