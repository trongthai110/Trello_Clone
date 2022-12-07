package ndtt.trelloclone.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.nav_header_main.*
import ndtt.trelloclone.R
import ndtt.trelloclone.firebase.FirestoreClass
import ndtt.trelloclone.models.User
import ndtt.trelloclone.utils.Constants
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    private var selectedImageFileUri: Uri? = null
    private lateinit var userDetails: User
    private var profileImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setUpActionBar()

        FirestoreClass().loadUserData(this)

        ivProfileUserImage.setOnClickListener {

            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                    showImageChooser()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        btnUpdate.setOnClickListener {
            if (selectedImageFileUri != null) {
                uploadUserImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImageChooser()
            }
        } else {
            Toast.makeText(
                this,
                "Oops, you just denied the permission for storage. You can also allow it from settings.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbarMyProfileActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.my_profile_title)
        }

        toolbarMyProfileActivity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun showImageChooser() {
        var galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        resultLauncher.launch(galleryIntent)
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            if (data!!.data != null) {
                selectedImageFileUri = data?.data

                try {
                    Glide
                        .with(this@MyProfileActivity)
                        .load(selectedImageFileUri)
                        .centerCrop()
                        .placeholder(R.drawable.ic_user_place_holder)
                        .into(ivProfileUserImage)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
    //---

    fun setUserDataInUI(user: User) {

        userDetails = user

        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(ivProfileUserImage)

        etName.setText(user.name)
        etEmail.setText(user.email)
        if (user.mobile != 0L) {
            etMobile.setText(user.mobile.toString())
        }
    }

    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()

        if (profileImageURL.isNotEmpty()
            && profileImageURL != userDetails.image) {
            userHashMap[Constants.IMAGE] = profileImageURL
        }

        if (etName.text.toString() != userDetails.name) {
            userHashMap[Constants.NAME] = etName.text.toString()
        }

        if (etMobile.text.toString() != userDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = etMobile.text.toString().toLong()
        }

        FirestoreClass().updateUserProfileData(this, userHashMap)
    }

    private fun uploadUserImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        if (selectedImageFileUri != null) {

            val ref : StorageReference =
                FirebaseStorage.getInstance().reference.child(
                    "USER_IMAGE" + System.currentTimeMillis()
                            + "." + Constants.getFileExtension(this, selectedImageFileUri))
            ref.putFile(selectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot ->
                Log.i(
                    "Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri ->
                    Log.i(
                        "Download Image URL", uri.toString())
                    profileImageURL = uri.toString()

                    updateUserProfileData()
                }
            }.addOnFailureListener {
                exception ->
                Toast.makeText(
                    this@MyProfileActivity,
                    exception.message,
                    Toast.LENGTH_LONG
                ).show()

                hideProgressDialog()
            }
        }
    }

    fun profileUpdateSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
}