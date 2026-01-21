package com.pab.niyyah.ui.profile

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.pab.niyyah.R // Pastikan import R ini sesuai package anda
import com.pab.niyyah.databinding.FragmentEditProfileBinding
import com.pab.niyyah.utils.ImageUtils
import java.io.ByteArrayOutputStream
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

    // Variabel untuk menyimpan URI gambar sementara dari galeri
    private var selectedImageUri: Uri? = null

    // 1. Setup Image Picker (Galeri)
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            // Tampilkan preview langsung di ImageView
            binding.ivAvatar.setImageURI(uri)
        }
    }

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

                    // --- LOAD GAMBAR BASE64 dengan ImageUtils ---
                    val photoString = document.getString("photoUrl")
                    ImageUtils.loadBase64Image(binding.ivAvatar, photoString)
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

        // Listener Klik Foto Profil untuk Ganti Foto
        binding.ivAvatar.setOnClickListener {
            openGallery()
        }

        // Listener Klik Text "Change Photo" (Jika ada di XML)
        // binding.tvChangePhoto.setOnClickListener { openGallery() }

        binding.btnSave.setOnClickListener {
            if (!isSaving) {
                saveProfile()
            }
        }
    }

    private fun openGallery() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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

    // --- FUNGSI KOMPRESI GAMBAR (Wajib ada biar Firestore tidak error) ---
    private fun encodeImageToBase64(uri: Uri): String? {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Kecilkan ukuran gambar (Resize) ke lebar 300px
            // Ini penting karena Firestore cuma muat 1MB per dokumen
            val previewWidth = 300
            val previewHeight = (bitmap.height * (300.0 / bitmap.width)).toInt()
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, true)

            // Kompres kualitas JPEG
            val baos = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos) // Kualitas 70%
            val bytes = baos.toByteArray()

            // Ubah ke String Base64
            return Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
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

        if (firstName.isEmpty()) {
            Toast.makeText(context, "Nama depan wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        isSaving = true
        binding.btnSave.isEnabled = false
        binding.btnSave.text = "Menyimpan..."

        // Data Teks
        val userData = hashMapOf<String, Any>(
            "firstName" to firstName,
            "lastName" to lastName,
            "username" to username,
            "gender" to gender,
            "nationality" to nationality,
            "birthDate" to birthDate,
            "phoneNumber" to phoneNumber
        )

        // --- LOGIKA SIMPAN GAMBAR ---
        // Jika user memilih foto baru, kompres lalu masukkan ke userData
        if (selectedImageUri != null) {
            val base64Image = encodeImageToBase64(selectedImageUri!!)
            if (base64Image != null) {
                userData["photoUrl"] = base64Image
            }
        }

        // Simpan ke Firestore
        db.collection("users").document(currentUser.uid)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                if (_binding == null) return@addOnSuccessListener

                isSaving = false
                binding.btnSave.isEnabled = true
                binding.btnSave.text = "Save Profile" // Kembalikan teks tombol
                Toast.makeText(context, "Profil berhasil disimpan!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener { e ->
                if (_binding == null) return@addOnFailureListener

                isSaving = false
                binding.btnSave.isEnabled = true
                binding.btnSave.text = "Save Profile"
                Toast.makeText(context, "Gagal menyimpan profil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}