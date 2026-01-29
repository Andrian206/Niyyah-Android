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
import com.pab.niyyah.data.TaskUiModel
import com.pab.niyyah.databinding.ItemHeaderDateBinding // Pastikan nama file XML header benar
import com.pab.niyyah.databinding.ItemTaskBinding

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onCheckboxClick: (Task) -> Unit,
    private val onHeaderClick: (String) -> Unit // Callback Klik Header
) : ListAdapter<TaskUiModel, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TaskUiModel.Header -> TYPE_HEADER
            is TaskUiModel.Item -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val binding = ItemHeaderDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            HeaderViewHolder(binding)
        } else {
            val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            TaskViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind(getItem(position) as TaskUiModel.Header)
            is TaskViewHolder -> holder.bind((getItem(position) as TaskUiModel.Item).task)
        }
    }

    inner class HeaderViewHolder(private val binding: ItemHeaderDateBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(header: TaskUiModel.Header) {
            binding.tvHeaderTitle.text = header.title
            binding.tvCount.text = "(${header.count})"

            // Rotasi Panah: Jika Expanded (Terbuka) panah ke Atas (180), Jika Tutup panah normal (0)
            binding.ivArrow.rotation = if (header.isExpanded) 180f else 0f

            binding.root.setOnClickListener {
                onHeaderClick(header.title)
            }
        }
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.tvTaskTitle.text = task.title

            // Tampilkan jam saja biar rapi, karena sudah ada header tanggal
            binding.tvTaskTime.text = if (task.time.isNotEmpty()) task.time else ""
            binding.tvTaskTime.isVisible = task.time.isNotEmpty()

            // Logic Checkbox & Coret Teks (Tetap sama)
            binding.cbTask.setOnCheckedChangeListener(null)
            binding.cbTask.isChecked = task.isCompleted

            if (task.isCompleted) {
                binding.tvTaskTitle.paintFlags = binding.tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvTaskTitle.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.darker_gray))
            } else {
                binding.tvTaskTitle.paintFlags = binding.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.tvTaskTitle.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black))
            }

            binding.cbTask.setOnClickListener { onCheckboxClick(task) }
            binding.root.setOnClickListener { onTaskClick(task) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TaskUiModel>() {
        override fun areItemsTheSame(oldItem: TaskUiModel, newItem: TaskUiModel): Boolean {
            return when {
                oldItem is TaskUiModel.Header && newItem is TaskUiModel.Header -> oldItem.title == newItem.title
                oldItem is TaskUiModel.Item && newItem is TaskUiModel.Item -> oldItem.task.id == newItem.task.id
                else -> false
            }
        }
        override fun areContentsTheSame(oldItem: TaskUiModel, newItem: TaskUiModel): Boolean {
            return oldItem == newItem
        }
    }
}