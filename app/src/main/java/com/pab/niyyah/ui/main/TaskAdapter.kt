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
            binding.tvTaskTime.text = "${task.dueDate}, ${task.time}"
            
            binding.tvTaskDescription.isVisible = task.details.isNotEmpty()
            binding.tvTaskDescription.text = task.details

            val context = binding.root.context
            
            if (task.isCompleted) {
                binding.ivCheckbox.setImageResource(R.drawable.ic_checkbox_checked)
                binding.tvTaskTitle.paintFlags = binding.tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvTaskTitle.setTextColor(ContextCompat.getColor(context, R.color.greyMD))
                binding.tvTaskTime.setTextColor(ContextCompat.getColor(context, R.color.greyL))
                binding.tvTaskDescription.setTextColor(ContextCompat.getColor(context, R.color.greyL))
                binding.root.alpha = 0.7f
            } else {
                binding.ivCheckbox.setImageResource(R.drawable.ic_checkbox_unchecked)
                binding.tvTaskTitle.paintFlags = binding.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.tvTaskTitle.setTextColor(ContextCompat.getColor(context, R.color.black))
                binding.tvTaskTime.setTextColor(ContextCompat.getColor(context, R.color.greyMD))
                binding.tvTaskDescription.setTextColor(ContextCompat.getColor(context, R.color.greyMD))
                binding.root.alpha = 1.0f
            }

            binding.root.setOnClickListener { onTaskClick(task) }
            binding.ivCheckbox.setOnClickListener { onCheckboxClick(task) }
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
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}