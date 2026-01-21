package com.pab.niyyah.ui.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pab.niyyah.R
import com.pab.niyyah.databinding.FragmentEditTaskBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditTaskFragment : Fragment() {

    private var _binding: FragmentEditTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val calendar = Calendar.getInstance()
    private var taskId: String? = null
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        taskId = arguments?.getString("taskId")

        setupUI()
        setupClickListeners()
        loadTaskData()
    }

    private fun setupUI() {
        val repeatOptions = arrayOf("Never", "Daily", "Weekly", "Monthly")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, repeatOptions)
        binding.spinnerRepeat.adapter = adapter
    }

    private fun loadTaskData() {
        val id = taskId ?: return
        
        db.collection("tasks").document(id).get()
            .addOnSuccessListener { document ->
                if (_binding == null) return@addOnSuccessListener
                
                if (document != null && document.exists()) {
                    binding.etTitle.setText(document.getString("title") ?: "")
                    binding.etDetails.setText(document.getString("details") ?: "")
                    binding.etDueDate.setText(document.getString("dueDate") ?: "")
                    binding.etTime.setText(document.getString("time") ?: "")

                    val repeat = document.getString("repeat") ?: "Never"
                    val repeatOptions = arrayOf("Never", "Daily", "Weekly", "Monthly")
                    val position = repeatOptions.indexOf(repeat)
                    if (position >= 0) {
                        binding.spinnerRepeat.setSelection(position)
                    }
                }
            }
            .addOnFailureListener { e ->
                if (_binding == null) return@addOnFailureListener
                Toast.makeText(context, "Gagal memuat data: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            if (!isLoading) findNavController().navigateUp()
        }

        binding.etDueDate.setOnClickListener {
            showDatePicker()
        }

        binding.etTime.setOnClickListener {
            showTimePicker()
        }

        binding.btnSave.setOnClickListener {
            if (!isLoading) updateTask()
        }

        binding.btnDelete.setOnClickListener {
            if (!isLoading) showDeleteConfirmation()
        }
    }

    // Di dalam CreateTaskFragment.kt

    private fun showDatePicker() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                // GANTI Locale.getDefault() MENJADI Locale.US
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                binding.etDueDate.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

// Lakukan hal yang sama untuk EditTaskFragment.kt juga!

    private fun showTimePicker() {
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                binding.etTime.setText(timeFormat.format(calendar.time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }
    
    private fun setLoading(loading: Boolean) {
        isLoading = loading
        binding.btnSave.isEnabled = !loading
        binding.btnDelete.isEnabled = !loading
    }
    
    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Task")
            .setMessage("Apakah Anda yakin ingin menghapus task ini?")
            .setPositiveButton("Hapus") { _, _ -> deleteTask() }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateTask() {
        val title = binding.etTitle.text.toString().trim()
        val details = binding.etDetails.text.toString().trim()
        val dueDate = binding.etDueDate.text.toString().trim()
        val time = binding.etTime.text.toString().trim()
        val repeat = binding.spinnerRepeat.selectedItem.toString()

        if (title.isEmpty()) {
            binding.etTitle.error = "Judul task harus diisi"
            binding.etTitle.requestFocus()
            return
        }

        val id = taskId ?: return
        
        setLoading(true)

        val taskData = mapOf(
            "title" to title,
            "details" to details,
            "dueDate" to dueDate,
            "time" to time,
            "repeat" to repeat
        )

        db.collection("tasks").document(id).update(taskData)
            .addOnSuccessListener {
                if (_binding == null) return@addOnSuccessListener
                setLoading(false)
                Toast.makeText(context, "Task berhasil diupdate!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener { e ->
                if (_binding == null) return@addOnFailureListener
                setLoading(false)
                Toast.makeText(context, "Gagal update task: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteTask() {
        val id = taskId ?: return
        
        setLoading(true)
        
        db.collection("tasks").document(id).delete()
            .addOnSuccessListener {
                if (_binding == null) return@addOnSuccessListener
                setLoading(false)
                Toast.makeText(context, "Task berhasil dihapus!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener { e ->
                if (_binding == null) return@addOnFailureListener
                setLoading(false)
                Toast.makeText(context, "Gagal hapus task: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
