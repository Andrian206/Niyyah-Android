package com.pab.niyyah.ui.main

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pab.niyyah.R
import com.pab.niyyah.data.Task
import com.pab.niyyah.databinding.ItemTaskBinding

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onCheckboxClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val taskList = ArrayList<Task>()

    fun submitList(list: List<Task>) {
        taskList.clear()
        taskList.addAll(list)
        notifyDataSetChanged()
    }

    inner class TaskViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.tvTaskTitle.text = task.title
            binding.tvTaskTime.text = "${task.dueDate}, ${task.time}"
            
            // Set description jika ada
            if (task.details.isNotEmpty()) {
                binding.tvTaskDescription.visibility = android.view.View.VISIBLE
                binding.tvTaskDescription.text = task.details
            } else {
                binding.tvTaskDescription.visibility = android.view.View.GONE
            }

            // Update tampilan berdasarkan status completed
            if (task.isCompleted) {
                // Checkbox checked
                binding.ivCheckbox.setImageResource(R.drawable.ic_checkbox_checked)
                
                // Strikethrough dan warna abu-abu untuk task selesai
                binding.tvTaskTitle.paintFlags = binding.tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvTaskTitle.setTextColor(ContextCompat.getColor(binding.root.context, R.color.greyMD))
                binding.tvTaskTime.setTextColor(ContextCompat.getColor(binding.root.context, R.color.greyL))
                binding.tvTaskDescription.setTextColor(ContextCompat.getColor(binding.root.context, R.color.greyL))
                
                // Opacity lebih rendah
                binding.root.alpha = 0.7f
            } else {
                // Checkbox unchecked
                binding.ivCheckbox.setImageResource(R.drawable.ic_checkbox_unchecked)
                
                // Normal styling untuk task ongoing
                binding.tvTaskTitle.paintFlags = binding.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.tvTaskTitle.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black))
                binding.tvTaskTime.setTextColor(ContextCompat.getColor(binding.root.context, R.color.greyMD))
                binding.tvTaskDescription.setTextColor(ContextCompat.getColor(binding.root.context, R.color.greyMD))
                
                // Opacity normal
                binding.root.alpha = 1.0f
            }

            // Klik item -> Edit Task
            binding.root.setOnClickListener { onTaskClick(task) }
            
            // Klik checkbox -> Toggle completed
            binding.ivCheckbox.setOnClickListener { onCheckboxClick(task) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(taskList[position])
    }

    override fun getItemCount() = taskList.size
}