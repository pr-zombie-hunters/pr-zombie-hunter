import { useEffect, useState, useCallback } from 'react'
import { fetchPullRequests, fetchHunterActions, PullRequest, HunterAction } from './api'
import ZombieCard from './components/ZombieCard'
import './App.css'

export default function App() {
  const [prs, setPrs] = useState<PullRequest[]>([])
  const [actionsMap, setActionsMap] = useState<Record<string, HunterAction[]>>({})
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    try {
      const prList = await fetchPullRequests()
      setPrs(prList)
      const allActions = await fetchHunterActions()
      const map: Record<string, HunterAction[]> = {}
      allActions.forEach(a => {
        if (!map[a.prId]) map[a.prId] = []
        map[a.prId].push(a)
      })
      setActionsMap(map)
    } catch (e) {
      console.error(e)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load() }, [load])

  const alive    = prs.filter(p => p.zombieGrade !== 'DEFEATED')
  const revived  = prs.filter(p => p.zombieGrade === 'REVIVED')
  const defeated = prs.filter(p => p.zombieGrade === 'DEFEATED')
  const totalDamage = Object.values(actionsMap).flat().length * 5000

  return (
    <div className="app">
      <header className="top-bar">
        <div className="logo">
          <span className="logo-emoji">🧟</span>
          <div>
            <div className="logo-title">PR ZOMBIE HUNTER</div>
            <div className="logo-sub">HP MONSTER SYSTEM · DASHBOARD</div>
          </div>
        </div>
        <div className="stats">
          <div className="stat">
            <span className="stat-value alive">{alive.length}</span>
            <span className="stat-label">생존 중</span>
          </div>
          <div className="stat">
            <span className="stat-value revived">{revived.length}</span>
            <span className="stat-label">부활(Revert)</span>
          </div>
          <div className="stat">
            <span className="stat-value defeated">{defeated.length}</span>
            <span className="stat-label">처치완료</span>
          </div>
          <div className="stat">
            <span className="stat-value damage">{totalDamage.toLocaleString()}</span>
            <span className="stat-label">총 데미지</span>
          </div>
        </div>
      </header>

      <div className="rules-bar">
        <span>🔴 6시간마다 HP × 2 성장</span>
        <span>💬 코멘트 1개 = 5,000 데미지</span>
        <span>⏱ 1인 1회 데미지만 인정</span>
        <span>📧 1시간마다 이메일 알림</span>
        <span>🔄 Revert 시 처치 직전 HP로 부활</span>
      </div>

      <main className="main">
        {loading ? (
          <div className="loading">불러오는 중...</div>
        ) : (
          <>
            <div className="section-title">⚠️ 생존 중인 좀비 ({alive.length})</div>
            {alive.length === 0 ? (
              <div className="empty">🎉 현재 생존 중인 좀비 PR이 없습니다!</div>
            ) : (
              alive.map(pr => (
                <ZombieCard
                  key={pr.id}
                  pr={pr}
                  actions={actionsMap[pr.id] ?? []}
                  onAttack={load}
                />
              ))
            )}
          </>
        )}
      </main>
    </div>
  )
}
