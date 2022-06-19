package com.bangkit.storyapp.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bangkit.storyapp.MainActivity
import com.bangkit.storyapp.R
import com.bangkit.storyapp.data.model.UserModel
import com.bangkit.storyapp.data.remote.response.ResultResponse
import com.bangkit.storyapp.databinding.ActivityNewStoryBinding
import com.bangkit.storyapp.helper.Helper
import com.bangkit.storyapp.ui.view_model.AddNewStoryViewModel
import com.bangkit.storyapp.ui.view_model.ViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class NewStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewStoryBinding

    private lateinit var userModel: UserModel
    private var getFile: File? = null
    private var result: Bitmap? = null
    private var location: Location? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val viewModel: AddNewStoryViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()

        userModel = intent.getParcelableExtra(EXTRA_USER)!!
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getPermission()
        btnActivity()
    }

    private fun btnActivity() {
        binding.btnCamera.setOnClickListener { startCameraX() }
        binding.btnChooseImg.setOnClickListener { startGallery() }
        binding.btnUploadStory.setOnClickListener { uploadStory() }
        binding.onOffLoc.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                getMyLoc()
            } else {
                location = null
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Helper.showToastLong(this, getString(R.string.str_cant_get_perm))
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getPermission() {
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarNewStory)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        finish()
        return true
    }

    private fun startCameraX() {
        launcherIntentCameraX.launch(Intent(this, CameraActivity::class.java))
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = it.data?.getSerializableExtra("picture") as File
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean

            getFile = myFile
            result =
                Helper.rotateBitmap(
                    BitmapFactory.decodeFile(getFile?.path),
                    isBackCamera
                )
        }
        binding.imgStory.setImageBitmap(result)
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val selectedImg: Uri = it.data?.data as Uri
            val myFile = Helper.uriToFile(selectedImg, this)
            getFile = myFile
            binding.imgStory.setImageURI(selectedImg)
        }
    }

    private fun uploadStory() {
        when {
            binding.inpDescStory.editText?.text.toString().isEmpty() -> {
                binding.inpDescStory.error = getString(R.string.empty_desc)
            }
            getFile != null -> {
                val file = Helper.reduceFileImage(getFile as File)
                val description = binding.inpDescStory.editText?.text.toString()
                    .toRequestBody("application/json;charset=utf-8".toMediaType())
                val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imageMultipart = MultipartBody.Part.createFormData(
                    "photo",
                    file.name,
                    requestImageFile
                )
                var lat: RequestBody? = null
                var lon: RequestBody? = null
                if (location != null) {
                    lat = location?.latitude.toString().toRequestBody("text/plain".toMediaType())
                    lon = location?.longitude.toString().toRequestBody("text/plain".toMediaType())
                }

                // upload story
                viewModel.postStory(userModel.token, description, imageMultipart, lat, lon)
                    .observe(this) {
                        if (it != null) {
                            when (it) {
                                is ResultResponse.Loading -> {
                                    binding.progressBarNewStory.visibility = View.VISIBLE
                                }
                                is ResultResponse.Success -> {
                                    binding.progressBarNewStory.visibility = View.GONE
                                    Helper.showToast(this, getString(R.string.upload_success))
                                    val moveToListStoryActivity = Intent(this, MainActivity::class.java)
                                    moveToListStoryActivity.putExtra(MainActivity.EXTRA_USER, userModel)
                                    startActivity(moveToListStoryActivity)
                                    finish()
                                }
                                is ResultResponse.Error -> {
                                    binding.progressBarNewStory.visibility = View.GONE
                                    Helper.showToast(this, getString(R.string.upload_failed))
                                }
                            }
                        }
                    }
            }
            else -> {
                Helper.showToast(this, getString(R.string.no_file_chosen))
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getMyLoc() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    location = it
                    Log.d(TAG, "Lat : ${it.latitude}, Lon : ${it.longitude}")
                } else {
                    Helper.showToastLong(this, getString(R.string.enable_loc))
                    binding.onOffLoc.isChecked = false
                }
            }
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        Log.d(TAG, "$it")
        if (it[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            getMyLoc()
        } else binding.onOffLoc.isChecked = false
    }

    companion object {
        const val CAMERA_X_RESULT = 200
        private const val TAG = "AddStoryActivity"
        const val EXTRA_USER = "user"

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

}