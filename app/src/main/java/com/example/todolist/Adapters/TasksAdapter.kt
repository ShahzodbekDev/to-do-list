package com.example.todolist.Adapters

import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.Models.TasksModel
import com.example.todolist.R
import com.example.todolist.SQLiteDatabase.TasksDbManager
import com.example.todolist.SQLiteDatabase.TasksSQLiteHelper
import com.example.todolist.databinding.TasksItemBinding


class MyDiff() : DiffUtil.ItemCallback<TasksModel>() {
    override fun areItemsTheSame(oldItem: TasksModel, newItem: TasksModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TasksModel, newItem: TasksModel): Boolean {
        return oldItem == newItem
    }

}

class TasksAdapter(private val context: Context) :
    ListAdapter<TasksModel, TasksAdapter.TasksViewHolder>(MyDiff()) {


    private var taskLitener: TaskLitener? = null

    inner class TasksViewHolder(private val binding: TasksItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.deleteBtn.setOnClickListener {
                taskLitener?.onDeleteTask(adapterPosition)
            }
            binding.uploadBtn.setOnClickListener {
                taskLitener?.updateTask(tasksModel = getItem(adapterPosition))
            }
            binding.checkBox.setOnClickListener {
                val id = getItem(adapterPosition).id
                if (binding.checkBox.isChecked) {
                    taskLitener?.isChecked(id!!)

                } else {
                    taskLitener?.unChecked(id!!)
                }

            }
        }

        fun bind(tasksModel: TasksModel) = with(binding) {

            if (tasksModel.IsChecked == true) {
                checkBox.isChecked = true
            } else if (tasksModel.IsChecked == false) {
                checkBox.isChecked = false
            }




            taskText.text = root.context.getString(R.string.task_text, tasksModel.Tasktitle)
            checkBox.setOnCheckedChangeListener { _, isChecked ->


                if (isChecked) {

                    taskText.paintFlags = taskText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {

                    taskText.paintFlags = taskText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }

            }

            if (tasksModel.IsChecked == true) {
                taskText.paintFlags = taskText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }


            val hourType = if (tasksModel.Addhour < 12) "AM" else "PM"
            taskTime.text = root.context.getString(
                R.string.add_task_time,
                tasksModel.Addhour,
                tasksModel.Addminute,
                hourType
            )
        }
    }

    fun setTaskLitener(Litener: TaskLitener) {
        this.taskLitener = Litener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val binding =
            TasksItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TasksViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


}
