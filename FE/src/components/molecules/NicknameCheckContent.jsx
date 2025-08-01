const NicknameCheckContent = ({ nickname }) => {

  return (
    <>
      <h2 style={{ color: '#404040', marginBottom: '12px' }}>닉네임 중복 체크</h2>
      <p>입력하신 닉네임 {nickname}은 사용 가능합니다.</p>
    </>
  )
}

export default NicknameCheckContent