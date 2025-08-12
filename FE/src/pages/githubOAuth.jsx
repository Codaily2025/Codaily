import { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'

const GithubOAuth = () => {
    const navigate = useNavigate()
    const [searchParams] = useSearchParams()

    useEffect(() => {
        const status = searchParams.get('github')
        const error = searchParams.get('error')

        console.log('GitHub OAuth 팝업 콜백 - status:', status, 'error:', error)

        if (error) {
            console.error('GitHub OAuth 에러:', error)
            // 팝업을 닫고 부모 창 새로고침
            if (window.opener) {
                window.opener.location.reload()
                window.close()
            } else {
                navigate('/additional-info', { replace: true })
            }
            return
        }

        if (status === 'connected') {
            console.log('GitHub 연동 성공!')
            
            // 팝업인 경우: 부모 창을 GitHub 연동 완료 상태로 리다이렉트하고 팝업 닫기
            if (window.opener) {
                window.opener.location.href = '/additional-info?github=connected'
                window.close()
            } else {
                // 직접 접근한 경우: 바로 리다이렉트
                navigate('/additional-info?github=connected', { replace: true })
            }
        } else {
            console.log('GitHub 미연동 상태')
            
            // 팝업인 경우: 부모 창 새로고침 후 팝업 닫기
            if (window.opener) {
                window.opener.location.reload()
                window.close()
            } else {
                navigate('/additional-info', { replace: true })
            }
        }
    }, [searchParams, navigate])

    return (
        <div style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            height: '100vh',
            flexDirection: 'column'
        }}>
            <div>GitHub 연동 처리 중...</div>
            <div style={{ marginTop: '20px' }}>
                <div className="spinner"></div>
            </div>
            <style>{`
                .spinner {
                width: 40px;
                height: 40px;
                border: 4px solid #f3f3f3;
                border-top: 4px solid #3498db;
                border-radius: 50%;
                animation: spin 1s linear infinite;
                }
                
                @keyframes spin {
                0% { transform: rotate(0deg); }
                100% { transform: rotate(360deg); }
                }
            `}</style>
        </div>
    )

}

export default GithubOAuth