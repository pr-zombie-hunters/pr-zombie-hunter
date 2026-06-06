package com.zombie.notifier.service

import com.zombie.notifier.repository.HunterRepository
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailNotificationService(
    private val hunterRepository: HunterRepository,
    private val mailSender: JavaMailSender,
) {
    fun sendMonsterStatusEmail() {
        hunterRepository.findAll().forEach { hunter ->
            // TODO: 이메일 없는 사용자 스킵 로직 구현 필요 (TDD Green 단계)
            mailSender.send(SimpleMailMessage().apply {
                setTo(hunter.email)
                subject = "[PR Zombie Hunter] 몬스터 현황 알림"
                text = "현재 생존 중인 몬스터 현황입니다."
            })
        }
    }
}
