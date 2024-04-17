package com.example.todolist.Activities

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todolist.Adapters.CalendarAdapter
import com.example.todolist.Adapters.TaskLitener

import com.example.todolist.Adapters.TasksAdapter
import com.example.todolist.Models.TasksModel
import com.example.todolist.R
import com.example.todolist.SQLiteDatabase.TasksDbManager
import com.example.todolist.SQLiteDatabase.TasksSQLiteHelper
import com.example.todolist.databinding.ActivityMainBinding
import com.example.todolist.databinding.AddTaskDialogViewBinding
import com.example.todolist.databinding.UpdateDialogViewBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val tasksList = mutableListOf<TasksModel>()
    private lateinit var dbManager: TasksDbManager
    lateinit var tasksAdapter: TasksAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        





//      TasksSaveData
        dbManager = TasksDbManager(this)
        dbManager.onCreate()
        tasksAdapter = TasksAdapter(this)

        
//   Add Task Dialog
        val currentTime = Calendar.getInstance()
        val addTimeHour = currentTime.get(Calendar.HOUR_OF_DAY)
        val addTimeMinute = currentTime.get(Calendar.MINUTE)

        binding.addTask.setOnClickListener {
            val dialogViewBinding = AddTaskDialogViewBinding.inflate(layoutInflater, null, false)
            val dialog = AlertDialog.Builder(this)
                .setView(dialogViewBinding.root)
                .create()
            dialogViewBinding.addTaskBtn.setOnClickListener {
                val taskTitle = dialogViewBinding.enteriTask.text




               if (taskTitle.isEmpty()) {
                   Toast.makeText(this, "Enter task", Toast.LENGTH_SHORT).show()
               } else{
                   dbManager.insertTask(
                       TasksModel(
                           Tasktitle = taskTitle.toString(),
                           Addhour = addTimeHour,
                           Addminute = addTimeMinute,
                           IsChecked = false
                       )
                   )
               }
                tasksAdapter.submitList(fetchTasks())
                tasksAdapter.notifyItemInserted(tasksList.size)
                dialog.dismiss()
            }
            dialog.show()
        }

        fetchTasks()
        tasksAdapter.submitList(tasksList)
        val myLayoutManager = LinearLayoutManager(this)
        binding.tasksView.apply {
            adapter = tasksAdapter
            layoutManager = myLayoutManager
        }

        
//      TaskListener
        tasksAdapter.setTaskLitener(object : TaskLitener {
            override fun onDeleteTask(position: Int) {
                tasksList[position].id?.let { dbManager.deleteTask(it) }
                tasksAdapter.submitList(fetchTasks())
                tasksAdapter.notifyItemRemoved(position)
            }

            override fun isChecked(id: Int) {
                dbManager.updateIsChecked(id, true)
                tasksAdapter.submitList(fetchTasks())
                tasksAdapter.notifyItemInserted(id)

            }

            override fun unChecked(id: Int) {
                dbManager.updateIsChecked(id, false)
                tasksAdapter.submitList(fetchTasks())
                tasksAdapter.notifyItemInserted(id)
            }

            override fun updateTask(tasksModel: TasksModel) {
                // Tanlangan vazifani yangilash uchun qo'llanma dialogini ochish
                val dialogViewBinding = UpdateDialogViewBinding.inflate(layoutInflater, null, false)
                val dialog = AlertDialog.Builder(this@MainActivity)
                    .setView(dialogViewBinding.root)
                    .create()

                // Qo'llanma oynasida tanlangan vazifa sarlavhasini joylashtirish
                dialogViewBinding.updateTaskTitle.text = tasksModel.Tasktitle

                // Qo'llanma oynasida vazifa matnini joylashtirish
                dialogViewBinding.updateTask.setText(tasksModel.Tasktitle)

                // Yangilash tugmasi bosilganda
                dialogViewBinding.updateTaskBtn.setOnClickListener {
                    // Vazifa sarlavhasini o'zgartirish
                    val taskTitle = dialogViewBinding.updateTask.text.toString()

                    // Yangilangan vazifani yangilash
                    val updatedTasksModel = tasksModel.copy(
                        Tasktitle = taskTitle,
                        Addhour = addTimeHour,
                        Addminute = addTimeMinute,
                        IsChecked = false
                    )
                    dbManager.updateTask(updatedTasksModel)

                    // Yangilangan vazifalarni qayta yuklash
                    tasksAdapter.submitList(fetchTasks())
                    tasksAdapter.notifyItemRangeChanged(tasksList.indexOf(tasksModel), tasksModel.id!!)


                    // Qo'llanmani yopish
                    dialog.dismiss()
                }
                // Qo'llanmani ko'rsatish
                dialog.show()
            }


        })


//     CalendarView
        val recyclerView = binding.recyclerView
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        val daysInMonth = getDaysInMonth(month, year)
        val startingDay = getStartingDay(month, year)
        val adapter = CalendarAdapter(daysInMonth, startingDay)
        recyclerView.adapter = adapter


    }

//  SQLite all data fetch
    private fun fetchTasks(): List<TasksModel> {
        val cursor = dbManager.fetch()
        tasksList.clear()
        if (cursor != null) {
            val idIndex = cursor.getColumnIndex(TasksSQLiteHelper.id)
            val titleIndex = cursor.getColumnIndex(TasksSQLiteHelper.TaskTitle)
            val timeHourIndex = cursor.getColumnIndex(TasksSQLiteHelper.AddTimeHour)
            val timeMinuteIndex = cursor.getColumnIndex(TasksSQLiteHelper.AddTimeMinute)
            val isCheckedIndex = cursor.getColumnIndex(TasksSQLiteHelper.IsChecked)

            do {
                val id = cursor.getInt(idIndex)
                val title = cursor.getString(titleIndex)
                val timeHour = cursor.getInt(timeHourIndex)
                val timeMinute = cursor.getInt(timeMinuteIndex)
                val isChecked = cursor.getInt(isCheckedIndex) != 0

                val tasksModel = TasksModel(id, title, timeHour, timeMinute, isChecked)
                Log.d("TAG", "onCreate: $id $title $timeHour $timeMinute $isChecked")


                tasksList.add(tasksModel)

            } while (cursor.moveToNext())

        }
        return tasksList
    }

    
    // Methods
    private fun getDaysInMonth(month: Int, year: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun getStartingDay(month: Int, year: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)
        return calendar.get(Calendar.DAY_OF_WEEK)
    }

    //     SearchBar
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.search_menu, menu)
//        val searchItem = menu.findItem(R.id.action_search)
//        val searchView = searchItem?.actionView as SearchView
//        searchView.queryHint = "Qidirish"
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                // Qidiruv tugadi
//                return true
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                if (newText.isNullOrEmpty()) {
//                    // Agar so'z bo'sh yoki null bo'lsa, ishlatuvchi hech narsa yozmagan
//                    return false
//                }
//
//                val filteredList = tasksList.filter { task ->
//                    task.Tasktitle.contains(newText, ignoreCase = true) // Yangi so'zni saralanayotgan ma'lumotlarda qidirish
//                }
//
//                tasksAdapter.submitList(filteredList) // Filterlangan ma'lumotlarni RecyclerView ga yuborish
//
//
//                return true // Bu funksiya to'xtatilmayapti, u o'zgartirilgan ma'lumotlarni RecyclerView ga yuboradi
//            }
//
//        })
//        return super.onCreateOptionsMenu(menu)
//    }

}


// https://www.figma.com/file/P1dMBW8eJB1ehEe1erG0l2/To-do-list-(Community)?node-id=0%3A1&mode=dev