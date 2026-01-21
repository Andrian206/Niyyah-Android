package com.pab.niyyah.ui.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pab.niyyah.R
import com.pab.niyyah.databinding.FragmentCreateTaskBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateTaskFragment : Fragment() {

    private var _binding: FragmentCreateTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val calendar = Calendar.getInstance()
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        val repeatOptions = arrayOf("Never", "Daily", "Weekly", "Monthly")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, repeatOptions)
        binding.spinnerRepeat.adapter = adapter
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

        binding.btnCreate.setOnClickListener {
            if (!isLoading) createTask()
        }
    }

    // Di dalam CreateTaskFragment.kt

    private fun showDatePicker() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
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
        binding.btnCreate.isEnabled = !loading
        binding.btnCreate.text = if (loading) "Membuat..." else getString(R.string.create)
    }

    private fun createTask() {
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

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "User tidak ditemukan!", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        val taskData = hashMapOf(
            "title" to title,
            "details" to details,
            "dueDate" to dueDate,
            "time" to time,
            "repeat" to repeat,
            "isCompleted" to false,
            "userId" to currentUser.uid,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("tasks").add(taskData)
            .addOnSuccessListener {
                if (_binding == null) return@addOnSuccessListener
                setLoading(false)
                Toast.makeText(context, "Task berhasil dibuat!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener { e ->
                if (_binding == null) return@addOnFailureListener
                setLoading(false)
                Toast.makeText(context, "Gagal membuat task: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
