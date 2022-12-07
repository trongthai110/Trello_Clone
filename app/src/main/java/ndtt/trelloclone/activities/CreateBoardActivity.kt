package ndtt.trelloclone.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_create_board.*
import kotlinx.android.synthetic.main.activity_my_profile.*
import ndtt.trelloclone.R
import ndtt.trelloclone.firebase.FirestoreClass
import ndtt.trelloclone.models.Board
import ndtt.trelloclone.utils.Constants
import java.io.IOException

class CreateBoardActivity : BaseActivity() {

    private var selectedImageFileUri: Uri? = null

    private lateinit var userName: String

    private var boardImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)

        setupActionBar()

        if (intent.hasExtra(Constants.NAME)) {
            userName = intent.getStringExtra(Constants.NAME).toString()
        }

        ivBoardImage.setOnClickListener {
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

        btnCreate.setOnClickListener {
            if (selectedImageFileUri != null) {
                uploadBoardImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }

    }

    private fun createBoard() {
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserId())

        var board = Board(
            etBoardName.text.toString(),
            boardImageURL,
            userName,
            assignedUsersArrayList
        )

        FirestoreClass().createBoard(this, board)
    }

    private fun uploadBoardImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        val ref : StorageReference =
            FirebaseStorage.getInstance().reference.child(
                "BOARD_IMAGE" + System.currentTimeMillis()
                        + "." + Constants.getFileExtension(this, selectedImageFileUri))
        ref.putFile(selectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot ->
            Log.i(
                " Board img URL",
                taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
            )

            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri ->
                Log.i(
                    "Download Image URL", uri.toString())
                boardImageURL = uri.toString()

                createBoard()
            }
        }.addOnFailureListener {
                exception ->
            Toast.makeText(
                this,
                exception.message,
                Toast.LENGTH_LONG
            ).show()

            hideProgressDialog()
        }
    }

    fun boardCreatedSuccessfully() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)

        finish()
    }



    private fun setupActionBar() {

        setSupportActionBar(toolbarCreateBoardActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.create_board_title)
        }

        toolbarCreateBoardActivity.setNavigationOnClickListener { onBackPressed() }
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
                        .with(this)
                        .load(selectedImageFileUri)
                        .centerCrop()
                        .placeholder(R.drawable.ic_board_place_holder)
                        .into(ivBoardImage)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}