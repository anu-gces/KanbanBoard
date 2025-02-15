package com.example.kanbanboard.model

data class Task(
    val taskTitle: String,
    var taskColumn: String // "To-Do", "In Progress", "Completed"
)

