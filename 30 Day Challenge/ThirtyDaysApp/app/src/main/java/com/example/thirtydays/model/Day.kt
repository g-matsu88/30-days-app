package com.example.thirtydays.model

/**
 * Represents a single day of the 30-day challenge.
 */
data class Day(
    val dayNumber: Int,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false
)
