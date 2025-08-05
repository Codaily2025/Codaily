import { defaultInstance, authInstance } from "./axios"

const project_default_url = 'projects/'

export const getActiveProjects = async () => {
    try {
        const { data } = await defaultInstance.get(
            '...',
        )
        return data

    } catch (error) {
        console.log(error)
    }
}