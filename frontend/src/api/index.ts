import axios from 'axios'

const api = axios.create({
  baseURL: '', // Vite 프록시 통해서 요청 (/api → localhost:8090)
  timeout: 5000,
})

export interface PullRequest {
  id: string
  title: string
  author: string
  zombieGrade: string
  staleDays: number
  lastActivityAt: string
}

export interface HunterAction {
  id: number
  prId: string
  hunterId: string
  actionType: string
  createdAt: string
}

export interface HunterStat {
  hunterId: string
  huntCount: number
}

export const fetchPullRequests = () =>
  api.get<PullRequest[]>('/api/pull-requests').then(r => r.data)

export const fetchHunterActions = (prId?: string) =>
  api.get<HunterAction[]>('/api/hunter-actions', { params: prId ? { prId } : {} }).then(r => r.data)

export const fetchHunterStats = () =>
  api.get<HunterStat[]>('/api/hunter-actions/stats').then(r => r.data)

export const postHunterAction = (prId: string, hunterId: string) =>
  api.post<HunterAction>('/api/hunter-actions', { prId, hunterId, actionType: 'HUNT' }).then(r => r.data)

// prId 형식: "owner/repo#number" ex) "pr-zombie-hunters/pr-zombie-hunter#21"
const parsePrId = (prId: string) => {
  const [repoFull, number] = prId.split('#')
  const [owner, repo] = repoFull.split('/')
  return { owner, repo, number }
}

export const mergePullRequest = (prId: string) => {
  const { owner, repo, number } = parsePrId(prId)
  return api.post(`/api/pull-requests/${owner}/${repo}/${number}/merge`).then(r => r.data)
}

export const closePullRequest = (prId: string) => {
  const { owner, repo, number } = parsePrId(prId)
  return api.post(`/api/pull-requests/${owner}/${repo}/${number}/close`).then(r => r.data)
}
