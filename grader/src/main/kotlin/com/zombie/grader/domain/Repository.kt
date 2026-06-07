package com.zombie.grader.domain

import jakarta.persistence.*

@Entity
@Table(name = "repositories")
class Repository(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 100)
    val owner: String,

    @Column(nullable = false, length = 100)
    val name: String,

    @Column(nullable = false, length = 50)
    val sector: String = "DEFAULT",

    @Column(nullable = false)
    val tracked: Boolean = true,
)
