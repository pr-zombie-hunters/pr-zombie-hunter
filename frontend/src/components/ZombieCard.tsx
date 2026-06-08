import { useState } from 'react'
import { PullRequest, HunterAction, postHunterAction, mergePullRequest, closePullRequest } from '../api'
import './ZombieCard.css'

interface Props {
  pr: PullRequest
  actions: HunterAction[]
  onAttack: () => void
}

const HP_MAP: Record<string, number> = {
  ZOMBIE: 10000,
  ANCIENT: 20000,
  LICH: 40000,
  BOSS: 80000,
  NONE: 10000,
}

const GRADE_EMOJI: Record<string, string> = {
  ZOMBIE: '🧟',
  ANCIENT: '💀',
  LICH: '🔮',
  BOSS: '👹',
  NONE: '🧟',
}

export default function ZombieCard({ pr, actions, onAttack }: Props) {
  const [loading, setLoading] = useState(false)
  const [mergeLoading, setMergeLoading] = useState(false)
  const [closeLoading, setCloseLoading] = useState(false)
  const isRevived = pr.zombieGrade === 'REVIVED'
  const maxHp = HP_MAP[pr.zombieGrade] ?? 10000
  const currentHp = Math.max(0, maxHp - actions.length * 5000)
  const hpPercent = Math.round((currentHp / maxHp) * 100)
  const requiredComments = Math.ceil(currentHp / 5000)
  const growthCount = Math.floor(pr.staleDays / 6)
  const attackers = [...new Set(actions.map(a => a.hunterId))]

  const handleAttack = async () => {
    setLoading(true)
    try {
      await postHunterAction(pr.id, 'me')
      onAttack()
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={`zombie-card ${isRevived ? 'revived' : ''}`}>
      {isRevived && (
        <div className="revert-banner">
          ⚠️ REVERT 감지! 좀비 부활 — HP {currentHp.toLocaleString()}으로 복원됨
        </div>
      )}

      <div className="card-header">
        <div className="pr-meta">
          <span className="pr-number">PR #{pr.id.split('#')[1] ?? pr.id}</span>
          <span className="pr-author"> · @{pr.author}</span>
        </div>
        <div className="grade-emoji">{GRADE_EMOJI[pr.zombieGrade] ?? '🧟'}</div>
      </div>

      <div className="pr-title">{pr.title}</div>

      <div className="hp-section">
        <div className="hp-label">
          <span className="hp-current" style={{ color: isRevived ? '#7c3aed' : '#e53e3e' }}>
            {currentHp.toLocaleString()} HP
          </span>
          <span className="hp-max">최대 {maxHp.toLocaleString()}</span>
        </div>
        <div className="hp-bar-bg">
          <div className={`hp-bar ${isRevived ? 'revived' : ''}`} style={{ width: `${hpPercent}%` }} />
          <span className="hp-percent">{hpPercent}%</span>
        </div>
        <div className="hp-info">
          <span>성장 {growthCount}회</span>
          <span>처치까지 코멘트 {requiredComments}개 필요</span>
          <span>공격자 {attackers.length}명</span>
        </div>
      </div>

      {attackers.length > 0 && (
        <div className="attackers">
          {attackers.map(a => (
            <span key={a} className="attacker-badge">⚔ {a}</span>
          ))}
          <button className="contrib-btn">📊 기여 현황</button>
        </div>
      )}

      <div className="card-actions">
        <button className="attack-btn" onClick={handleAttack} disabled={loading}>
          ● 코멘트 공격 (-5,000 HP)
        </button>
        <button
          className="merge-btn"
          disabled={mergeLoading}
          onClick={async () => {
            if (!confirm(`PR "${pr.title}" 을 머지할까요?`)) return
            setMergeLoading(true)
            try {
              await mergePullRequest(pr.id)
              alert('머지 완료! 🎉')
              onAttack()
            } catch {
              alert('머지 실패. GitHub에서 직접 확인해주세요.')
            } finally {
              setMergeLoading(false)
            }
          }}
        >
          {mergeLoading ? '...' : 'Merge ✓'}
        </button>
        <button
          className="close-btn"
          disabled={closeLoading}
          onClick={async () => {
            if (!confirm(`PR "${pr.title}" 을 닫을까요?`)) return
            setCloseLoading(true)
            try {
              await closePullRequest(pr.id)
              alert('PR 닫기 완료!')
              onAttack()
            } catch {
              alert('닫기 실패. GitHub에서 직접 확인해주세요.')
            } finally {
              setCloseLoading(false)
            }
          }}
        >
          {closeLoading ? '...' : 'Close X'}
        </button>
      </div>
    </div>
  )
}
