package com.pab.niyyah.ui.profile

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.pab.niyyah.databinding.FragmentEditProfileBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val calendar = Calendar.getInstance()
    private var isSaving = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadUserData()
        setupClickListeners()
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser ?: return
        
        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                if (_binding == null) return@addOnSuccessListener
                
                if (document != null && document.exists()) {
                    binding.etFirstName.setText(document.getString("firstName") ?: "")
                    binding.etLastName.setText(document.getString("lastName") ?: "")
                    binding.etUsername.setText(document.getString("username") ?: "")
                    binding.etGender.setText(document.getString("gender") ?: "")
                    binding.etNationality.setText(document.getString("nationality") ?: "")
                    binding.etBirthDate.setText(document.getString("birthDate") ?: "")
                    binding.etPhone.setText(document.getString("phoneNumber") ?: "")
                }
            }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.etBirthDate.setOnClickListener {
            showDatePicker()
        }

        binding.ivCalendar.setOnClickListener {
            showDatePicker()
        }

        binding.btnSave.setOnClickListener {
            if (!isSaving) {
                saveProfile()
            }
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.etBirthDate.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveProfile() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val gender = binding.etGender.text.toString().trim()
        val nationality = binding.etNationality.text.toString().trim()
        val birthDate = binding.etBirthDate.text.toString().trim()
        val phoneNumber = binding.etPhone.text.toString().trim()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "User tidak ditemukan!", Toast.LENGTH_SHORT).show()
            return
        }

        // Validasi minimal
        if (firstName.isEmpty()) {
            Toast.makeText(context, "Nama depan wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        isSaving = true
        binding.btnSave.isEnabled = false

        val userData = hashMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "username" to username,
            "gender" to gender,
            "nationality" to nationality,
            "birthDate" to birthDate,
            "phoneNumber" to phoneNumber
        )

        // Gunakan set dengan merge untuk menghindari error jika document belum ada
        db.collection("users").document(currentUser.uid)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                if (_binding == null) return@addOnSuccessListener
                
                isSaving = false
                binding.btnSave.isEnabled = true
                Toast.makeText(context, "Profil berhasil disimpan!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener { e ->
                if (_binding == null) return@addOnFailureListener
                
                isSaving = false
                binding.btnSave.isEnabled = true
                Toast.makeText(context, "Gagal menyimpan profil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}