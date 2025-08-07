// src/apis/user.js
export const fetchUserProfile = async (token) => {
  const response = await fetch('http://localhost:8080/api/me', {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!response.ok) {
    throw new Error('사용자 정보를 가져오는데 실패했습니다.');
  }
  return response.json();
};