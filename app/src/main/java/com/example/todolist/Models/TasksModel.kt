package com.example.todolist.Models

data class TasksModel(
    val id: Int? = 0,
    val Tasktitle: String,
    val Addhour: Int,
    val Addminute: Int,
    val IsChecked: Boolean
) {

}
