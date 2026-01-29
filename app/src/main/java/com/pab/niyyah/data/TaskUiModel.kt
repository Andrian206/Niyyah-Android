package com.pab.niyyah.data

sealed interface TaskUiModel {
    data class Header(
        val title: String,
        val isExpanded: Boolean,
        val count: Int
    ) : TaskUiModel {
        val id = title
    }

    data class Item(val task: Task) : TaskUiModel {
        val id = task.id
    }
}