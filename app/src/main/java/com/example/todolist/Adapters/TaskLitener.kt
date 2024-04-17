package com.example.todolist.Adapters

import com.example.todolist.Models.TasksModel

interface TaskLitener {
    fun onDeleteTask(position: Int)
    fun isChecked(id: Int)
    fun unChecked(id: Int)
    fun updateTask(tasksModel: TasksModel)


}