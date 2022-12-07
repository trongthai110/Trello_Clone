package ndtt.trelloclone.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_splash.*
import ndtt.trelloclone.R
import ndtt.trelloclone.firebase.FirestoreClass

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //if/else de set full man hinh tuy theo phien ban SDK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        //typeface = font family
        val typeFace: Typeface = Typeface.createFromAsset(assets, "FredokaOne.ttf")
        tvAppName.typeface = typeFace

        //xu ly chuyen sang man hinh Intro
        Handler(Looper.getMainLooper()).postDelayed( {
                var currentUserId = FirestoreClass().getCurrentUserId()

                if (currentUserId.isNotEmpty()) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                } else {
                    startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
                }
                // Call this when your activity is done and should be closed.
                finish()
            }, 2500
        )
    }
}