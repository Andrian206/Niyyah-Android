package com.pab.niyyah.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ImageView
import com.pab.niyyah.R

/**
 * Utility class untuk menangani operasi gambar Base64 secara konsisten
 * di seluruh aplikasi.
 */
object ImageUtils {

    /**
     * Decode string Base64 menjadi Bitmap dan set ke ImageView.
     * Jika gagal atau string kosong, akan menampilkan placeholder default.
     *
     * @param imageView Target ImageView untuk menampilkan gambar
     * @param base64String String Base64 dari Firestore
     * @param placeholderResId Resource ID untuk placeholder (default: ic_avatar_placeholder)
     */
    fun loadBase64Image(
        imageView: ImageView,
        base64String: String?,
        placeholderResId: Int = R.drawable.ic_avatar_placeholder
    ) {
        if (base64String.isNullOrEmpty()) {
            imageView.setImageResource(placeholderResId)
            return
        }

        try {
            // Skip jika ini adalah URL (bukan Base64)
            if (base64String.startsWith("http")) {
                imageView.setImageResource(placeholderResId)
                return
            }

            // Decode Base64 ke byte array
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)

            // Decode dengan options untuk mengoptimalkan memori
            val options = BitmapFactory.Options().apply {
                // Pertama, hanya baca dimensi gambar tanpa load ke memori
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size, options)

            // Hitung sample size untuk mengoptimalkan memori
            options.inSampleSize = calculateInSampleSize(options, 300, 300)
            options.inJustDecodeBounds = false

            // Decode bitmap yang sesungguhnya
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size, options)

            if (bitmap != null) {
                // Crop ke bentuk persegi (square) untuk memastikan lingkaran sempurna
                val squareBitmap = cropToSquare(bitmap)
                imageView.setImageBitmap(squareBitmap)

                // Recycle bitmap asli jika berbeda dengan hasil crop
                if (squareBitmap != bitmap && !bitmap.isRecycled) {
                    bitmap.recycle()
                }
            } else {
                imageView.setImageResource(placeholderResId)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            imageView.setImageResource(placeholderResId)
        }
    }

    /**
     * Hitung sample size optimal untuk decode bitmap.
     * Ini membantu mengurangi penggunaan memori.
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Crop bitmap menjadi bentuk persegi (square) dari bagian tengah.
     * Ini memastikan gambar akan mengisi lingkaran dengan sempurna
     * tanpa ada ruang kosong atau distorsi.
     */
    private fun cropToSquare(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // Jika sudah persegi, langsung return
        if (width == height) {
            return bitmap
        }

        // Tentukan sisi terpendek sebagai ukuran persegi
        val size = minOf(width, height)

        // Hitung posisi awal crop (dari tengah)
        val x = (width - size) / 2
        val y = (height - size) / 2

        return Bitmap.createBitmap(bitmap, x, y, size, size)
    }
}
