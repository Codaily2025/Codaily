// src/queries/usegitHub.js
import { useQuery } from '@tanstack/react-query';
import { fetchGithubId } from '../apis/gitHub';

export const useGithubIdQuery = () => {
    return useQuery({
        queryKey: ['githubId'],
        queryFn: fetchGithubId,
    });
};