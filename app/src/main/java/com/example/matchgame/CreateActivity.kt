package com.example.matchgame

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.matchgame.models.BoardSize
import com.example.matchgame.utils.BitmapScaler
import com.example.matchgame.utils.EXTRA_BOARD_SIZE
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

class CreateActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CreateActivity"
        private const val PICK_PHOTO_CODE = 343
        private const val MAX_GAME_NAME_LENGTH = 14
        private const val MIN_GAME_NAME_LENGTH = 3
    }
    private lateinit var rvImagePicker: RecyclerView
    private lateinit var etGameName: EditText
    private lateinit var btnCreate: Button
    private lateinit var pbUploading: ProgressBar

    private lateinit var adapter: ImagePickerAdapter
    private lateinit var boardSize: BoardSize
    private var numImagesRequired = -1
    private val chosenImageUris = mutableListOf<Uri>()
    private val storage = Firebase.storage
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.clCreateGame)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize

        numImagesRequired = boardSize.getNumOfPairs()
        rvImagePicker = findViewById(R.id.rvImagePicker)
        etGameName = findViewById(R.id.etGameName)
        btnCreate = findViewById(R.id.btnCreate)
        pbUploading = findViewById(R.id.pbUploading)

        btnCreate.setOnClickListener{
            saveDataToFirebase()
        }
        etGameName.filters = arrayOf(InputFilter.LengthFilter(MAX_GAME_NAME_LENGTH))

        etGameName.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                btnCreate.isEnabled = enableCreateButton()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {}

        })

        adapter = ImagePickerAdapter(this, chosenImageUris, boardSize, object: ImagePickerAdapter.ImageClickListener {
            override fun onPlaceHolderClicked() {
                launchIntentForPhotos()
            }

        })
        rvImagePicker.adapter = adapter
        rvImagePicker.setHasFixedSize(true)
        rvImagePicker.layoutManager = GridLayoutManager(this, boardSize.getWidth())

        btnCreate.isEnabled = enableCreateButton()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != PICK_PHOTO_CODE || resultCode != Activity.RESULT_OK || data == null) {
            Log.w(TAG, "Did not get data back from the launched activity, user likely canceled flow")
            return
        }
        val selectedURi = data.data
        val clipData = data.clipData

        if(clipData != null) {
            Log.i(TAG, "CLipData numImages ${clipData.itemCount}: $clipData")
            for (i in 0 until clipData.itemCount) {
                val clipItem = clipData.getItemAt(i)
                if(chosenImageUris.size < numImagesRequired) {
                    chosenImageUris.add(clipItem.uri)
                }
            }
        } else if (selectedURi != null) {
            Log.i(TAG, "data: $selectedURi")
            chosenImageUris.add(selectedURi)
        }
        adapter.notifyDataSetChanged()

    }

    private fun launchIntentForPhotos() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_pictures)), PICK_PHOTO_CODE)
    }
    private fun enableCreateButton(): Boolean {
        //check we cna enable saved button
        if(chosenImageUris.size != numImagesRequired) {
            return false

        }
        if(etGameName.text.isBlank() || etGameName.text.length < MIN_GAME_NAME_LENGTH) {
            return false
        }

        return true
    }

    private fun saveDataToFirebase() {
        Log.i(TAG, "saveDataToFirebase")
        btnCreate.isEnabled = false

        val customGameName = etGameName.text.toString().trim()
        //Check if there is a game with this name in the database
        db.collection("games").document(customGameName).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    AlertDialog.Builder(this)
                        .setTitle(getString(R.string.name_taken))
                        .setMessage(getString(R.string.game_name_exists, customGameName))
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                    btnCreate.isEnabled = true
                } else {
                    handleAllImagesUpload(customGameName)
                }
            } else {
                Log.e(TAG, "Encountered error while saving memory game", task.exception)
                Toast.makeText(this, getString(R.string.error_saving_game), Toast.LENGTH_SHORT).show()
                btnCreate.isEnabled = true
            }
        }.addOnFailureListener {
            Log.e(TAG, "Failed to check if game exists in the database", it)
            Toast.makeText(this, getString(R.string.failed_checking_game_exists), Toast.LENGTH_SHORT).show()
            btnCreate.isEnabled = true
        }
    }

    private fun handleAllImagesUpload(gameName: String) {
        pbUploading.visibility = View.VISIBLE
        var didEncounterError = false
        val uploadedImageUrl = mutableListOf<String>()

        for((index, photoUri) in chosenImageUris.withIndex()) {
            val imageByteArray = getImageByteArray(photoUri)
            val filePath = "images/$gameName/${System.currentTimeMillis()}-${index}.jpg"
            val photoReference = storage.reference.child(filePath)

            photoReference.putBytes(imageByteArray)
                .continueWithTask { photoUploadTask ->
                    Log.i(TAG, "Upload bytes: ${photoUploadTask.result?.bytesTransferred}")
                    photoReference.downloadUrl
                }.addOnCompleteListener{ downloadUrlTask ->
                    if(!downloadUrlTask.isSuccessful) {
                        Log.e(TAG, "Error with Firebase storage", downloadUrlTask.exception)
                        Toast.makeText(this, getString(R.string.failed_upload_image), Toast.LENGTH_SHORT).show()
                        didEncounterError = true
                        return@addOnCompleteListener
                    }
                    if(didEncounterError) {
                        pbUploading.visibility = View.GONE
                        return@addOnCompleteListener
                    }
                    val downloadUrl = downloadUrlTask.result.toString()
                    uploadedImageUrl.add(downloadUrl)
                    pbUploading.progress = uploadedImageUrl.size * 100 / chosenImageUris.size
                    Log.i(TAG, "Finish Uplaoding $photoUri, num uploaded ${uploadedImageUrl.size}")

                    if(uploadedImageUrl.size == chosenImageUris.size) {
                        handleAllImagesUpload(gameName, uploadedImageUrl)
                    }
                }
        }
    }

    private fun handleAllImagesUpload(gameName: String, imageUrls: MutableList<String>) {
        // Handle upload to Firebase
        db.collection("games").document(gameName)
            .set(mapOf("images" to imageUrls))
            .addOnCompleteListener { gameCreationTask ->
                pbUploading.visibility = View.GONE
                if (!gameCreationTask.isSuccessful) {
                    Log.e(TAG, getString(R.string.game_creation_failed), gameCreationTask.exception)
                    Toast.makeText(this, getString(R.string.upload_failed), Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }
                Log.i(TAG, "Successfully created game $gameName")
                AlertDialog.Builder(this)
                    .setMessage(getString(R.string.upload_complete, gameName))
                    .setPositiveButton(getString(R.string.ok)) { _, _ ->
                        val intent = Intent(this, MainMenuActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .show()
            }
    }


    private fun getImageByteArray(photoUri: Uri): ByteArray {
        val originalBitmap = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, photoUri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
        }
        Log.i(TAG, "Original width ${originalBitmap.width} and ${originalBitmap.height}")
        val scaleBitmap = BitmapScaler.scalertoFitHeight(originalBitmap, 250)

        Log.i(TAG, "Scaled width ${scaleBitmap.width} and ${scaleBitmap.height}")
        val byteOutputStream = ByteArrayOutputStream()
        scaleBitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteOutputStream)
        return byteOutputStream.toByteArray()
    }
}
