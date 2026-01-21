package com.pab.niyyah.ui.main

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pab.niyyah.R
import com.pab.niyyah.data.Task
import com.pab.niyyah.databinding.ItemTaskBinding

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onCheckboxClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    class TaskViewHolder(
        private val binding: ItemTaskBinding,
        private val onTaskClick: (Task) -> Unit,
        private val onCheckboxClick: (Task) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.tvTaskTitle.text = task.title

            // Logika Teks Waktu
            if (task.time.isNotEmpty()) {
                binding.tvTaskTime.text = if(task.dueDate.isNotEmpty()) "${task.dueDate}, ${task.time}" else task.time
            } else {
                binding.tvTaskTime.text = task.dueDate
            }

            // Hindari bug checkbox trigger listener saat scroll
            binding.cbTask.setOnCheckedChangeListener(null)
            binding.cbTask.isChecked = task.isCompleted

            // --- VISUAL SELESAI (CORET TEKS) ---
            if (task.isCompleted) {
                // Coret Teks
                binding.tvTaskTitle.paintFlags = binding.tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                // Warna Abu
                binding.tvTaskTitle.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.darker_gray))
            } else {
                // Hapus Coretan
                binding.tvTaskTitle.paintFlags = binding.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                // Warna Hitam/Normal
                binding.tvTaskTitle.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black))
            }

            // Listener Checkbox
            binding.cbTask.setOnClickListener {
                onCheckboxClick(task)
            }

            // Listener Klik Body (Edit)
            binding.root.setOnClickListener { onTaskClick(task) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding, onTaskClick, onCheckboxClick)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean = oldItem == newItem
    }
}