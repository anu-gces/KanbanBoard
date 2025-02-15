package com.example.kanbanboard

import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kanbanboard.adapter.TaskAdapter
import com.example.kanbanboard.model.Task
import androidx.recyclerview.widget.ItemTouchHelper
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts




private fun enableSwipeToDelete(recyclerView: RecyclerView, adapter: TaskAdapter) {
    val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false // No move action needed
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            adapter.removeTaskAt(position) // Remove task from list
        }
    }

    val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
    itemTouchHelper.attachToRecyclerView(recyclerView)
}

class MainActivity : AppCompatActivity() {

    private lateinit var todoAdapter: TaskAdapter
    private lateinit var inProgressAdapter: TaskAdapter
    private lateinit var completedAdapter: TaskAdapter

    private lateinit var notificationHelper: NotificationHelper

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                showNotification()
            }
        }


    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            showNotification()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun showNotification() {
        notificationHelper.showNotification("Task Reminder", "Don't forget to update your Kanban board!")
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        notificationHelper = NotificationHelper(this)
        requestNotificationPermission() // Automatically request and send notification


        val tasks: MutableList<Task> = mutableListOf(
            Task("Task 1", "To Do"),
            Task("Task 2", "To Do"),
            Task("Task 3", "To Do"),
            Task("Task 4", "In Progress"),
            Task("Task 5", "Completed"),
            Task("Task 6", "Completed")
        )



        val todoTasks = tasks.filter { it.taskColumn == "To Do" }.toMutableList()
        val inProgressTasks = tasks.filter { it.taskColumn == "In Progress" }.toMutableList()
        val completedTasks = tasks.filter { it.taskColumn == "Completed" }.toMutableList()

        todoAdapter = setupRecyclerView(R.id.recycler_todo, todoTasks)
        inProgressAdapter = setupRecyclerView(R.id.recycler_in_progress, inProgressTasks)
        completedAdapter = setupRecyclerView(R.id.recycler_completed, completedTasks)

        setDragListener(R.id.recycler_todo, todoAdapter)
        setDragListener(R.id.recycler_in_progress, inProgressAdapter)
        setDragListener(R.id.recycler_completed, completedAdapter)

        enableSwipeToDelete(findViewById(R.id.recycler_todo), todoAdapter)
        enableSwipeToDelete(findViewById(R.id.recycler_in_progress), inProgressAdapter)
        enableSwipeToDelete(findViewById(R.id.recycler_completed), completedAdapter)

        // For "To Do" tasks
        val tvAddTaskTodo: TextView = findViewById(R.id.tv_add_task_todo)
        val bottomSheet: View = findViewById(R.id.bottom_sheet)
        val btnAddTask: AppCompatButton = findViewById(R.id.btn_add_task) // Reference the Add Task Button
        val editText: EditText = findViewById(R.id.edit_task_input) // Reference the EditText
        var currentCategory = "To Do"  // Default category is "To Do"
        tvAddTaskTodo.setOnClickListener {
            currentCategory = "To Do"
            bottomSheetAnimation(bottomSheet)
        }
        btnAddTask.setOnClickListener {
            val taskTitle = editText.text.toString().trim() // Get text from EditText and trim whitespace
            if (taskTitle.isNotEmpty()) {
                // Add the task to the tasks list
                tasks.add(Task(taskTitle, currentCategory))

                // Update the filtered list for the selected category
                when (currentCategory) {
                    "To Do" -> todoTasks.add(Task(taskTitle, "To Do"))
                    "In Progress" -> inProgressTasks.add(Task(taskTitle, "In Progress"))
                    "Completed" -> completedTasks.add(Task(taskTitle, "Completed"))
                }

                // Notify the adapter that the dataset has changed
                when (currentCategory) {
                    "To Do" -> todoAdapter.notifyItemInserted(todoTasks.size - 1)
                    "In Progress" -> inProgressAdapter.notifyItemInserted(inProgressTasks.size - 1)
                    "Completed" -> completedAdapter.notifyItemInserted(completedTasks.size - 1)
                }

                // Call the animation function for the bottom sheet
                bottomSheetAnimation(bottomSheet)

                // Clear the input field after adding the task
                editText.text.clear()
            }
        }

        // For "In Progress" tasks
        val tvAddTaskInProgress: TextView = findViewById(R.id.tv_add_task_in_progress)
        tvAddTaskInProgress.setOnClickListener {
            currentCategory = "In Progress"

            bottomSheetAnimation(bottomSheet)
        }

        // For "Completed" tasks
        val tvAddTaskCompleted: TextView = findViewById(R.id.tv_add_task_completed)
        tvAddTaskCompleted.setOnClickListener {
            currentCategory = "Completed"
            bottomSheetAnimation(bottomSheet)

        }
    }

    private fun setupRecyclerView(recyclerId: Int, taskList: MutableList<Task>): TaskAdapter {
        val recyclerView: RecyclerView = findViewById(recyclerId)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = TaskAdapter(taskList) { view, task -> view.tag = task }
        recyclerView.adapter = adapter

        return adapter
    }

    private fun setDragListener(recyclerId: Int, adapter: TaskAdapter) {
        val recyclerView: RecyclerView = findViewById(recyclerId)

        recyclerView.setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    val task = event.localState as? Task
                    if (task != null) {
                        // Remove task from its previous column
                        when (task.taskColumn) {
                            "To Do" -> todoAdapter.removeTask(task)
                            "In Progress" -> inProgressAdapter.removeTask(task)
                            "Completed" -> completedAdapter.removeTask(task)
                        }

                        // Update task column
                        task.taskColumn = getColumnName(recyclerId)

                        // Get the target position where task should be inserted
                        val dropPosition = getDropPosition(recyclerView, event)

                        // Insert task at the correct position
                        adapter.addTaskAt(task, dropPosition)
                    }
                }
            }
            true
        }
    }

    // Determines the correct drop position based on the touch event
    private fun getDropPosition(recyclerView: RecyclerView, event: DragEvent): Int {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i)
            if (event.y < child.bottom) {
                return layoutManager.getPosition(child)
            }
        }
        return recyclerView.adapter?.itemCount ?: 0 // Default to last position if no match
    }


    private fun getColumnName(recyclerId: Int): String {
        return when (recyclerId) {
            R.id.recycler_todo -> "To Do"
            R.id.recycler_in_progress -> "In Progress"
            R.id.recycler_completed -> "Completed"
            else -> "To Do"
        }
    }

}
