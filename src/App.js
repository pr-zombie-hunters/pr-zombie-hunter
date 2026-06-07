import { useState, useEffect } from "react";

// 📡 API 함수 import
// 백엔드 연동 시 아래 주석 해제 후 더미 데이터 import 삭제
// import { fetchMonsters, attackMonster, mergeMonster, closeMonster } from "./api";
import { DUMMY_MONSTERS } from "./api"; // 더미 데이터 (백엔드 연동 시 삭제)

const DAMAGE = 5000;

// 🎨 공통 스타일
const S = {
  card: (defeated, reverted, danger) => ({
    background: defeated ? "#f8fafc" : reverted ? "#faf5ff" : danger ? "#fff5f5" : "#fff",
    border: `1px solid ${defeated ? "#e2e8f0" : reverted ? "#d8b4fe" : danger ? "#fca5a5" : "#e2e8f0"}`,
    borderRadius: 14, padding: 20, opacity: defeated ? 0.6 : 1,
    boxShadow: reverted ? "0 4px 16px #a855f720" : danger ? "0 4px 16px #ef444420" : "0 2px 8px #0000000a",
  }),
  btn: (color, bg, border) => ({
    padding: "9px 14px", background: bg, border: `1px solid ${border}`,
    borderRadius: 8, color, fontSize: 12, cursor: "pointer",
    fontFamily: "monospace", fontWeight: 600,
  }),
  mono: (size, color) => ({ fontSize: size, fontFamily: "monospace", color }),
};

// ⏳ 로딩 스피너
function LoadingSpinner() {
  return (
    <div style={{ display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", minHeight: "40vh", gap: 16 }}>
      <div style={{
        width: 48, height: 48,
        border: "4px solid #e2e8f0",
        borderTop: "4px solid #dc2626",
        borderRadius: "50%",
        animation: "spin 0.8s linear infinite",
      }} />
      <div style={{ ...S.mono(13, "#94a3b8"), letterSpacing: 2 }}>좀비 불러오는 중...</div>
      <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
    </div>
  );
}

// ❌ 에러 배너
function ErrorBanner({ message, onRetry }) {
  return (
    <div style={{
      background: "#fff5f5", border: "1px solid #fca5a5", borderRadius: 12,
      padding: "16px 20px", marginBottom: 24,
      display: "flex", alignItems: "center", justifyContent: "space-between",
    }}>
      <div>
        <div style={{ ...S.mono(13, "#dc2626"), fontWeight: "bold", marginBottom: 4 }}>⚠️ 오류 발생</div>
        <div style={{ ...S.mono(12, "#64748b") }}>{message}</div>
      </div>
      {/* 재시도 버튼 */}
      <button onClick={onRetry} style={S.btn("#dc2626", "#fff", "#fca5a5")}>
        🔄 재시도
      </button>
    </div>
  );
}

// 🔴 HP 바
function HPBar({ current, max, defeated, reverted }) {
  const pct = defeated ? 0 : Math.round(Math.max(0, Math.min(100, (current / max) * 100)));
  const color = reverted ? "#a855f7" : pct > 60 ? "#ef4444" : pct > 30 ? "#f97316" : "#eab308";
  return (
    <div>
      <div style={{ display: "flex", justifyContent: "space-between", ...S.mono(11, "#64748b"), marginBottom: 5 }}>
        <span style={{ fontWeight: "bold", color }}>{defeated ? "0" : current.toLocaleString()} HP</span>
        <span>최대 {max.toLocaleString()}</span>
      </div>
      <div style={{ position: "relative", height: 20, background: "#f1f5f9", borderRadius: 10, overflow: "hidden", border: "1px solid #e2e8f0" }}>
        <div style={{ width: `${pct}%`, height: "100%", background: `linear-gradient(90deg, ${color}bb, ${color})`, borderRadius: 10, transition: "width 0.6s ease" }} />
        <div style={{ position: "absolute", inset: 0, display: "flex", alignItems: "center", justifyContent: "center", ...S.mono(11, "#374151"), fontWeight: "bold" }}>
          {defeated ? "💀 DEFEATED" : `${pct}%`}
        </div>
      </div>
    </div>
  );
}

// 👥 팀원별 데미지 기여 현황
// 백엔드 연동 시: GraphQL damage_log 쿼리 결과를 damageLog props로 전달
// query { damageLog(monsterId: $id) { attacker_github_id damage_amount } }

function DamageContribution({ damageLog }) {
  if (!damageLog?.length) return null;
  const grouped = damageLog.reduce((acc, { attacker_github_id, damage_amount }) => {
    acc[attacker_github_id] = (acc[attacker_github_id] || 0) + damage_amount;
    return acc;
  }, {});
  const total = Object.values(grouped).reduce((a, b) => a + b, 0);
  return (
    <div style={{ marginTop: 14, background: "#f8fafc", borderRadius: 10, padding: "12px 14px", border: "1px solid #e2e8f0" }}>
      <div style={{ ...S.mono(11, "#94a3b8"), letterSpacing: 1, marginBottom: 10 }}>⚔️ 팀원별 데미지 기여 현황</div>
      {Object.entries(grouped).sort((a, b) => b[1] - a[1]).map(([id, dmg]) => {
        const pct = Math.round((dmg / total) * 100);
        return (
          <div key={id} style={{ marginBottom: 8 }}>
            <div style={{ display: "flex", justifyContent: "space-between", ...S.mono(11, "#374151"), marginBottom: 3 }}>
              <span>@{id}</span>
              <span style={{ color: "#ea580c", fontWeight: "bold" }}>{dmg.toLocaleString()} ({pct}%)</span>
            </div>
            <div style={{ height: 6, background: "#e2e8f0", borderRadius: 3, overflow: "hidden" }}>
              <div style={{ width: `${pct}%`, height: "100%", background: "linear-gradient(90deg, #fb923c, #ea580c)", borderRadius: 3, transition: "width 0.6s ease" }} />
            </div>
          </div>
        );
      })}
      <div style={{ ...S.mono(10, "#94a3b8"), textAlign: "right", marginTop: 6 }}>총 누적 데미지: {total.toLocaleString()}</div>
    </div>
  );
}

// 🧟 몬스터 카드
function MonsterCard({ monster: m, onAttack, onMerge, onClose, actionLoading }) {
  const [showLog, setShowLog] = useState(false);
  const hpPct = m.max_hp > 0 ? (m.current_hp / m.max_hp) * 100 : 0;
  const danger = hpPct > 80 && !m.is_reverted;
  const emoji = m.is_defeated ? "☠️" : m.is_reverted ? "🧬" : danger ? "👹" : "🧟";

    // 버튼 로딩 중 여부 (백엔드 요청 중 중복 클릭 방지)
    const isLoading = actionLoading === m.id;

  return (
    <div style={S.card(m.is_defeated, m.is_reverted, danger)}>
      {/* Revert 부활 배너 */}
      {m.is_reverted && (
        <div style={{ background: "linear-gradient(135deg, #f3e8ff, #ede9fe)", border: "1px solid #c4b5fd", borderRadius: 8, padding: "8px 12px", marginBottom: 12, ...S.mono(12, "#7c3aed"), fontWeight: "bold" }}>
          ⚠️ REVERT 감지! 좀비 부활 — HP {m.hp_before_defeat?.toLocaleString()} 으로 복원됨
        </div>
      )}

      {/* PR 정보 */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 14 }}>
        <div>
          <div style={{ ...S.mono(11, "#94a3b8"), marginBottom: 4 }}>PR #{m.pr_number} · @{m.author}</div>
          <div style={{ fontSize: 15, fontWeight: 700, color: m.is_defeated ? "#94a3b8" : "#1e293b" }}>
            {m.is_defeated ? "💀 " : m.is_reverted ? "🔮 " : danger ? "🔴 " : "🧟 "}{m.pr_title}
          </div>
        </div>
        <span style={{ fontSize: 28 }}>{emoji}</span>
      </div>

      {/* HP 바 */}
      <HPBar current={m.current_hp} max={m.max_hp} defeated={m.is_defeated} reverted={m.is_reverted} />

      {/* 통계 */}
      <div style={{ display: "flex", justifyContent: "space-between", marginTop: 10, ...S.mono(11, "#94a3b8") }}>
        <span>성장 {m.growth_count}회</span>
        {!m.is_defeated && <span>처치까지 코멘트 {Math.ceil(m.current_hp / DAMAGE)}개 필요</span>}
        <span>공격자 {m.attackers.length}명</span>
      </div>

      {/* 공격자 배지 + 기여 현황 토글 */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginTop: 10 }}>
        <div style={{ display: "flex", gap: 6, flexWrap: "wrap" }}>
          {m.attackers.map((a) => (
            <span key={a} style={{ background: "#fff7ed", border: "1px solid #fed7aa", borderRadius: 20, padding: "2px 10px", ...S.mono(11, "#ea580c") }}>⚔️ {a}</span>
          ))}
        </div>
        <button onClick={() => setShowLog(!showLog)} style={{ background: "none", border: "1px solid #e2e8f0", borderRadius: 6, padding: "3px 10px", ...S.mono(11, "#64748b"), cursor: "pointer", marginLeft: 8 }}>
          {showLog ? "▲ 숨기기" : "📊 기여 현황"}
        </button>
      </div>

      {/* 팀원별 데미지 기여 현황 */}
      {showLog && <DamageContribution damageLog={m.damage_log} />}

      {/* 액션 버튼 */}
      {!m.is_defeated && (
        <div style={{ display: "flex", gap: 8, marginTop: 14 }}>
          {/*
           * 백엔드 연동: POST /api/monsters/{id}/attack
           * 백엔드에서 처리: comment_id 중복 확인, damage_log 저장
           */}
          <button
            onClick={() => onAttack(m.id)}
            disabled={isLoading}
            style={{
              flex: 1, padding: "9px 0",
              background: isLoading ? "#94a3b8" : m.is_reverted
                ? "linear-gradient(135deg,#7c3aed,#a855f7)"
                : "linear-gradient(135deg,#dc2626,#ef4444)",
              border: "none", borderRadius: 8, color: "#fff",
              ...S.mono(12, "#fff"), fontWeight: "bold",
              cursor: isLoading ? "not-allowed" : "pointer",
              opacity: isLoading ? 0.7 : 1,
            }}
          >
            {isLoading ? "⏳ 처리 중..." : "💬 코멘트 공격 (-5,000 HP)"}
          </button>
          {/* 백엔드 연동: POST /api/monsters/{id}/merge */}
          <button onClick={() => onMerge(m.id)} disabled={isLoading} style={{ ...S.btn("#16a34a", "#f0fdf4", "#86efac"), opacity: isLoading ? 0.5 : 1, cursor: isLoading ? "not-allowed" : "pointer" }}>Merge ✓</button>
          {/* 백엔드 연동: POST /api/monsters/{id}/close */}
          <button onClick={() => onClose(m.id)} disabled={isLoading} style={{ ...S.btn("#64748b", "#f8fafc", "#cbd5e1"), opacity: isLoading ? 0.5 : 1, cursor: isLoading ? "not-allowed" : "pointer" }}>Close ✗</button>
        </div>
      )}
    </div>
  );
}

// 🏠 메인 App

// 백엔드 담당 기능

// 1. 이메일 알림
//    - 1시간마다 Scheduler 실행
//    - 생존 중인 몬스터 목록 발송

// 2. comment_id 저장
//    - GitHub Comment ID 저장
//    - 중복 데미지 방지

// 3. last_grown_at 저장
//    - 마지막 HP 성장 시각

// 4. Redis hp_before_defeat
//    - Merge/Close 시 저장
//    - Revert 시 복구
// 프론트는 API 응답을 화면에 표시만 함
export default function App() {
  const [monsters, setMonsters] = useState([]);

  // ✅ 추가: 로딩 / 에러 / 액션 로딩 상태
    const [loading, setLoading] = useState(true);       // 최초 데이터 로딩
  const [error, setError] = useState(null);            // 에러 메시지
  const [actionLoading, setActionLoading] = useState(null); // 현재 액션 중인 몬스터 id

  const [log, setLog] = useState([]);

  // ✅ 추가: 몬스터 목록 불러오기
  // 백엔드 연동 시: USE_DUMMY를 false로 변경
    const USE_DUMMY = true; // ← 백엔드 연동 시 false로 변경

  const loadMonsters = async () => {
    setLoading(true);
    setError(null);
    try {
      if (USE_DUMMY) {
        // 더미 데이터 사용 (백엔드 연동 전)
        setMonsters(DUMMY_MONSTERS);
      } else {
        // 백엔드 연동 시 주석 해제
        // const data = await fetchMonsters();
        // setMonsters(data);
      }
    } catch (err) {
      setError(err.message || "데이터를 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  };

    // ✅ 추가: 최초 마운트 시 데이터 로딩
    useEffect(() => {
    loadMonsters();
  }, []);

  // ⏰ 몬스터 성장 시스템
  // 0시간 = 10,000
  // 3시간 = 20,000
  // 6시간 = 40,000
  // 12시간 = 80,000
  // 18시간 = 160,000
    useEffect(() => {
    const interval = setInterval(() => {
      setMonsters(prev =>
        prev.map(monster => {
          if (monster.is_defeated) return monster;

          //  PR 생성 후 경과 시간 계산
          const elapsedHours = Math.max(
            0,
            (Date.now() - new Date(monster.created_at).getTime()) /
              (1000 * 60 * 60)
          );

          let growthCount = 0;

          if (elapsedHours >= 18) {
            growthCount = 4;
          } else if (elapsedHours >= 12) {
            growthCount = 3;
          } else if (elapsedHours >= 6) {
            growthCount = 2;
          } else if (elapsedHours >= 3) {
            growthCount = 1;
          }

          const maxHp = monster.base_hp * Math.pow(2, growthCount);

          const totalDamage = monster.damage_log.reduce(
            (sum, d) => sum + d.damage_amount,
            0
          );

          const currentHp = Math.max(0, maxHp - totalDamage);

          return {
            ...monster,
            growth_count: growthCount,
            max_hp: maxHp,
            current_hp: currentHp,
          };
        })
      );
    }, 1000);

    return () => clearInterval(interval);
  }, []);

  const addLog = (msg) =>
    setLog((p) => [
      {
        msg,
        time: new Date().toLocaleTimeString(),
      },
      ...p.slice(0, 9),
    ]);

  // ⚔️ 공격 / Merge / Close
  // ✅ 추가: 백엔드 연동 시 API 호출 + 에러 처리
    const handleAction = async (id, type) => {
    // 중복 클릭 방지
    if (actionLoading) return;
    setActionLoading(id);

    try {
      if (!USE_DUMMY) {
                // 백엔드 연동 시 주석 해제
                // let updatedMonster;
        // if (type === "attack") {
        //   const currentUser = "me"; // GitHub OAuth 로그인 연동 시 실제 ID로 교체
        //   updatedMonster = await attackMonster(id, currentUser, crypto.randomUUID());
        // } else if (type === "merge") {
        //   updatedMonster = await mergeMonster(id);
        // } else if (type === "close") {
        //   updatedMonster = await closeMonster(id);
        // }
        // setMonsters(prev => prev.map(m => m.id === id ? updatedMonster : m));
      } else {
        // 더미 데이터 로컬 처리 (백엔드 연동 전)
        setMonsters((prev) =>
          prev.map((m) => {
            if (m.id !== id) return m;

            if (type === "attack") {
              const currentUser = "me";

              // damage_log를 조회하여 현재 사용자가 이미 공격했는지 확인
              // 1인당 한 PR에 1회만 데미지 인정
              const alreadyAttacked = m.damage_log.some(
                (d) => d.attacker_github_id === currentUser
              );

              if (alreadyAttacked) {
                addLog(`🚫 이미 PR #${m.pr_number}에 공격했습니다.`);
                return m;
              }

              const newHp = m.current_hp - DAMAGE;
              const defeated = newHp <= 0;

              addLog(
                `⚔️ PR #${m.pr_number} 5,000 데미지! ${
                  defeated
                    ? "💀 처치 완료!"
                    : `남은 HP: ${Math.max(0, newHp).toLocaleString()}`
                }`
              );

              return {
                ...m,
                current_hp: Math.max(0, newHp),
                is_defeated: defeated,
                hp_before_defeat: defeated ? m.current_hp : m.hp_before_defeat,
                attackers: [...m.attackers, currentUser],
                damage_log: [
                  ...m.damage_log,
                  {
                    // 백엔드 연동 시 GitHub Comment ID 저장
                    comment_id: crypto.randomUUID(),
                    attacker_github_id: currentUser,
                    damage_amount: DAMAGE,
                    attacked_at: new Date().toISOString(),
                  },
                ],
              };
            }

            addLog(
              type === "merge"
                ? `✅ PR #${m.pr_number} Merge! 즉시 처치!`
                : `🔒 PR #${m.pr_number} Close! 즉시 처치!`
            );

            return {
              ...m,
              current_hp: 0,
              is_defeated: true,
              // Revert 복구용
              hp_before_defeat: m.current_hp,
            };
          })
        );
      }
    } catch (err) {
            // ✅ 추가: 액션 실패 시 에러 로그
            addLog(`❌ 요청 실패: ${err.message}`);
    } finally {
      setActionLoading(null);
    }
  };

  // 📊 통계
  const alive = monsters.filter((m) => !m.is_defeated);
  const defeated = monsters.filter((m) => m.is_defeated);
  const reverted = monsters.filter((m) => m.is_reverted && !m.is_defeated);
  const totalDamage = monsters.reduce(
    (s, m) => s + m.damage_log.reduce((a, d) => a + d.damage_amount, 0),
    0
  );

  const STATS = [
    { label: "생존 중", value: alive.length, color: "#ef4444", bg: "#fff5f5" },
    { label: "부활(Revert)", value: reverted.length, color: "#7c3aed", bg: "#faf5ff" },
    { label: "처치 완료", value: defeated.length, color: "#16a34a", bg: "#f0fdf4" },
    { label: "총 데미지", value: totalDamage.toLocaleString(), color: "#ea580c", bg: "#fff7ed" },
  ];

  const RULES = ["⏰ 6시간마다 HP × 2 성장", "💬 코멘트 1개 = 5,000 데미지", "🚫 1인 1회 데미지만 인정", "📧 1시간마다 이메일 알림", "🔮 Revert 시 처치 직전 HP로 부활"];

  return (
    <div style={{ minHeight: "100vh", background: "#f8fafc", color: "#1e293b", fontFamily: "'Courier New', monospace", padding: "0 0 60px" }}>
      <style>{`* { box-sizing: border-box; }`}</style>

      {/* 헤더 */}
      <div style={{ background: "#fff", borderBottom: "1px solid #e2e8f0", padding: "20px 32px", display: "flex", alignItems: "center", justifyContent: "space-between", boxShadow: "0 2px 8px #0000000a" }}>
        <div>
          <div style={{ fontSize: 22, fontWeight: 800, letterSpacing: 2, color: "#dc2626" }}>🧟 PR ZOMBIE HUNTER</div>
          <div style={{ ...S.mono(11, "#94a3b8"), marginTop: 2, letterSpacing: 2 }}>HP MONSTER SYSTEM · DASHBOARD</div>
        </div>
        <div style={{ display: "flex", gap: 16 }}>
          {STATS.map(({ label, value, color, bg }) => (
            <div key={label} style={{ textAlign: "center", background: bg, padding: "8px 14px", borderRadius: 10 }}>
              <div style={{ fontSize: 18, fontWeight: "bold", color }}>{value}</div>
              <div style={{ ...S.mono(10, "#94a3b8"), letterSpacing: 1 }}>{label}</div>
            </div>
          ))}
        </div>
      </div>

      <div style={{ maxWidth: 900, margin: "0 auto", padding: "32px 20px" }}>

        {/* 규칙 배너 */}
        <div style={{ background: "#fff", border: "1px solid #e2e8f0", borderRadius: 12, padding: "16px 20px", marginBottom: 28, display: "flex", gap: 20, flexWrap: "wrap", boxShadow: "0 1px 4px #0000000a" }}>
          {RULES.map((r) => <div key={r} style={S.mono(12, "#64748b")}>{r}</div>)}
        </div>

        {/* =============================================
            ✅ 추가: 로딩 스피너
            ============================================= */}
        {loading && <LoadingSpinner />}

        {/* =============================================
            ✅ 추가: 에러 배너 (재시도 버튼 포함)
            ============================================= */}
        {!loading && error && <ErrorBanner message={error} onRetry={loadMonsters} />}

        {/* 생존 중 */}
        {!loading && !error && alive.length > 0 && (
          <>
            <div style={{ ...S.mono(13, "#ef4444"), letterSpacing: 2, marginBottom: 14, fontWeight: "bold" }}>⚠ 생존 중인 좀비 ({alive.length})</div>
            <div style={{ display: "flex", flexDirection: "column", gap: 14, marginBottom: 32 }}>
              {alive.map((m) => (
                <MonsterCard
                  key={m.id}
                  monster={m}
                  onAttack={() => handleAction(m.id, "attack")}
                  onMerge={() => handleAction(m.id, "merge")}
                  onClose={() => handleAction(m.id, "close")}
                  actionLoading={actionLoading}
                />
              ))}
            </div>
          </>
        )}

        {/* 처치 완료 */}
        {!loading && !error && defeated.length > 0 && (
          <>
            <div style={{ ...S.mono(13, "#94a3b8"), letterSpacing: 2, marginBottom: 14, fontWeight: "bold" }}>☠ 처치 완료 ({defeated.length})</div>
            <div style={{ display: "flex", flexDirection: "column", gap: 12, marginBottom: 32 }}>
              {defeated.map((m) => (
                <MonsterCard
                  key={m.id}
                  monster={m}
                  onAttack={() => handleAction(m.id, "attack")}
                  onMerge={() => handleAction(m.id, "merge")}
                  onClose={() => handleAction(m.id, "close")}
                  actionLoading={actionLoading}
                />
              ))}
            </div>
          </>
        )}

        {/* 활동 로그 */}
        {log.length > 0 && (
          <div style={{ background: "#fff", border: "1px solid #e2e8f0", borderRadius: 12, padding: "16px 20px", boxShadow: "0 1px 4px #0000000a" }}>
            <div style={{ ...S.mono(12, "#94a3b8"), letterSpacing: 2, marginBottom: 12 }}>📋 활동 로그</div>
            {log.map((e, i) => (
              <div key={i} style={{ ...S.mono(12, i === 0 ? "#374151" : "#94a3b8"), marginBottom: 6 }}>
                <span style={{ color: "#cbd5e1", marginRight: 10 }}>{e.time}</span>{e.msg}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}