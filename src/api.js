// 📡 API 연동 모듈
// 백엔드 연동 시 BASE_URL을 실제 주소로 변경

const BASE_URL = process.env.REACT_APP_API_URL || "http://localhost:8080";

// 📦 더미 데이터 (백엔드 연동 시 삭제)
export const DUMMY_MONSTERS = [
  {
    id: 1,
    pr_title: "feat: 로그인 기능 추가",
    pr_number: 42,
    author: "kim_dev",
    base_hp: 10000,
    created_at: "2026-06-05T20:00:00",
    current_hp: 40000,
    max_hp: 40000,
    growth_count: 2,
    last_grown_at: "2026-06-06T08:00:00",
    attackers: ["lee_dev", "park_dev"],
    is_defeated: false,
    is_reverted: false,
    hp_before_defeat: null,
    damage_log: [
      {
        comment_id: "cmt_1001",
        attacker_github_id: "lee_dev",
        damage_amount: 5000,
        attacked_at: "2026-06-06T09:10:00",
      },
      {
        comment_id: "cmt_1002",
        attacker_github_id: "park_dev",
        damage_amount: 5000,
        attacked_at: "2026-06-06T09:30:00",
      },
    ],
  },
  {
    id: 2,
    pr_title: "fix: 버튼 스타일 수정",
    pr_number: 38,
    author: "park_dev",
    base_hp: 10000,
    created_at: "2026-06-05T23:00:00",
    current_hp: 15000,
    max_hp: 20000,
    growth_count: 1,
    attackers: ["kim_dev"],
    is_defeated: false,
    is_reverted: false,
    hp_before_defeat: null,
    damage_log: [{ attacker_github_id: "kim_dev", damage_amount: 5000 }],
  },
  {
    id: 3,
    pr_title: "refactor: DB 쿼리 최적화",
    pr_number: 35,
    author: "choi_dev",
    base_hp: 10000,
    created_at: "2026-06-05T08:00:00",
    current_hp: 72000,
    max_hp: 80000,
    growth_count: 3,
    attackers: ["kim_dev", "lee_dev", "park_dev", "choi_dev"],
    is_defeated: false,
    is_reverted: true,
    hp_before_defeat: 72000,
    damage_log: [
      { attacker_github_id: "kim_dev", damage_amount: 5000 },
      { attacker_github_id: "lee_dev", damage_amount: 5000 },
      { attacker_github_id: "park_dev", damage_amount: 5000 },
      { attacker_github_id: "choi_dev", damage_amount: 5000 },
    ],
  },
  {
    id: 4,
    pr_title: "chore: 패키지 버전 업데이트",
    pr_number: 30,
    author: "lee_dev",
    base_hp: 10000,
    created_at: "2026-06-05T14:00:00",
    current_hp: 0,
    max_hp: 40000,
    growth_count: 2,
    attackers: ["kim_dev", "park_dev"],
    is_defeated: true,
    is_reverted: false,
    hp_before_defeat: 40000,
    damage_log: [
      { attacker_github_id: "kim_dev", damage_amount: 5000 },
      { attacker_github_id: "park_dev", damage_amount: 5000 },
    ],
  },
];

// ✅ 몬스터 전체 목록 조회
// GET /api/monsters
// Response: Monster[]
export const fetchMonsters = async () => {
  const res = await fetch(`${BASE_URL}/api/monsters`);
  if (!res.ok) throw new Error(`몬스터 목록 조회 실패 (${res.status})`);
  return res.json();
};

// ⚔️ 몬스터 공격 (코멘트 데미지)
// POST /api/monsters/:id/attack
// Body: { attacker_github_id: string, comment_id: string }
// Response: Monster (업데이트된 몬스터)

export const attackMonster = async (id, attackerGithubId, commentId) => {
  const res = await fetch(`${BASE_URL}/api/monsters/${id}/attack`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      attacker_github_id: attackerGithubId,
      comment_id: commentId,
    }),
  });
  if (!res.ok) throw new Error(`공격 요청 실패 (${res.status})`);
  return res.json();
};

// ✅ PR Merge 처리
// POST /api/monsters/:id/merge
// Response: Monster (업데이트된 몬스터)
export const mergeMonster = async (id) => {
  const res = await fetch(`${BASE_URL}/api/monsters/${id}/merge`, {
    method: "POST",
  });
  if (!res.ok) throw new Error(`Merge 요청 실패 (${res.status})`);
  return res.json();
};

// 🔒 PR Close 처리
// POST /api/monsters/:id/close
// Response: Monster (업데이트된 몬스터)
export const closeMonster = async (id) => {
  const res = await fetch(`${BASE_URL}/api/monsters/${id}/close`, {
    method: "POST",
  });
  if (!res.ok) throw new Error(`Close 요청 실패 (${res.status})`);
  return res.json();
};