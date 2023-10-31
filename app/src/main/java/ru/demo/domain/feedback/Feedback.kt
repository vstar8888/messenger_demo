package ru.demo.domain.feedback

data class Feedback(val appName: String,
                    val companyTitle: String,
                    val email: String,
                    val phoneNumber: String,
                    val text: String)