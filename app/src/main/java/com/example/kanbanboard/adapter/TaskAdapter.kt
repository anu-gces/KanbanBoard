package com.example.kanbanboard.adapter

import android.content.ClipData
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kanbanboard.R
import com.example.kanbanboard.model.Task

class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val onDragStarted: (View, Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view, onDragStarted)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size

    // Add task to the list and notify the adapter
    fun addTask(task: Task) {
        tasks.add(task)
        notifyItemInserted(tasks.size - 1)
    }

    // Remove task from the list and notify the adapter
    fun removeTask(task: Task) {
        val position = getTaskPosition(task)
        if (position != -1) {
            tasks.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    // Get position of the task in the list
    private fun getTaskPosition(task: Task): Int {
        return tasks.indexOfFirst { it.taskTitle == task.taskTitle && it.taskColumn == task.taskColumn }
    }

    // Update the column of a task when moved
    fun updateTaskColumn(task: Task, newColumn: String) {
        val position = getTaskPosition(task)
        if (position != -1) {
            tasks[position].taskColumn = newColumn
            notifyItemChanged(position)
        }
    }

    fun addTaskAt(task: Task, position: Int) {
        val insertPosition = position.coerceIn(0, tasks.size) // Ensure valid index
        tasks.add(insertPosition, task)
        notifyItemInserted(insertPosition)
    }

    fun removeTaskAt(position: Int) {
        if (position in tasks.indices) {
            tasks.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    // ViewHolder to handle task view binding
    class TaskViewHolder(itemView: View, private val onDragStarted: (View, Task) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val taskTitle: TextView = itemView.findViewById(R.id.task_title)
        lateinit var task: Task // Add task property here

        fun bind(task: Task) {
            this.task = task // Set the task when binding
            taskTitle.text = task.taskTitle

            // Start drag when long-pressed
            itemView.setOnLongClickListener {
                val dragData = ClipData.newPlainText("Task", task.taskTitle)
                val shadow = View.DragShadowBuilder(itemView)
                onDragStarted(itemView, task)
                it.startDragAndDrop(dragData, shadow, task, 0)
                true
            }
        }
    }
}
