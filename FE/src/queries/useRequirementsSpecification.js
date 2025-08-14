import { fetchSpec } from '../apis/requirementsSpecification.js';
import { useQuery } from '@tanstack/react-query';

// 프로젝트Id가 있어야 요구사항 명세서 조회 가능
const isValidProjectId = (id) => {
  if (id === 0) return false;
  if (typeof id === 'number') return Number.isFinite(id) && id > 0;
  if (typeof id === 'string') {
    const s = id.trim();
    if (!s || s === 'null' || s === 'undefined') return false;
    const n = Number(s);
    return Number.isFinite(n) && n > 0;
  }
  return false;
};

export const useGetRequirementsSpecification = (projectId, opts = {}) => {
  const { polling = false, intervalMs = 1800 } = opts;
  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ['requirementsSpecification', projectId],
    queryFn: () => fetchSpec(projectId),
    enabled: isValidProjectId(projectId),
    placeholderData: (previousData) => previousData,  // 전 화면 깜빡임 최소화 (v5에서 keepPreviousData 대신 사용)
    refetchInterval: polling ? intervalMs : false,
    refetchIntervalInBackground: true,
    // 필요시 포커스시 재조회 방지하고 싶으면:
    // refetchOnWindowFocus: false,
  });
  // isLoading // 로딩 중일 때 로딩 표시
  // isError // 에러 발생 시 에러 표시
  // data // 요구사항 명세서 데이터
  // refetch // 수동으로 데이터를 다시 가져오는 함수
  return { data, isLoading, isError, refetch };
}