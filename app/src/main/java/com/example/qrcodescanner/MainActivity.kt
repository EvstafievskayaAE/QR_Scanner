package com.example.qrcodescanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var codeScanner: CodeScanner
    private val CAMERA_PERMISSION_CODE = 1111

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initCodeScanner()

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                Toast.makeText(
                    this,
                    "Scan result: ${it.text}", Toast.LENGTH_LONG
                ).show()
                openUrlInBrowser(it.text)
            }
        }

        codeScanner.errorCallback = ErrorCallback {
            runOnUiThread {
                Toast.makeText(
                    this,
                    "Camera error: ${it.message}", Toast.LENGTH_LONG
                ).show()
            }
        }

        checkCameraPermission()

        val reverseCameraImage: ImageView = reverseCameraImageView
        reverseCameraImage.setOnClickListener {
            reverseCamera()
            Toast.makeText(
                this,
                "Reverse camera", Toast.LENGTH_LONG
            ).show()
        }

        val cancelTextView: TextView = cancelTextView
        cancelTextView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    fun initCodeScanner(){
        codeScanner = CodeScanner(this, scannerView)
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false
    }

    fun reverseCamera() {
        if (codeScanner.camera == CodeScanner.CAMERA_BACK)
            codeScanner.camera = CodeScanner.CAMERA_FRONT
        else codeScanner.camera = CodeScanner.CAMERA_BACK
    }

    fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE
            )
        } else {
            codeScanner.startPreview()
        }
    }
    private fun isValidUrl(url: String): Boolean {
        return Patterns.WEB_URL.matcher(url).matches()
    }

    fun openUrlInBrowser(url: String) {
        if (isValidUrl(url)) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            if (browserIntent.resolveActivity(packageManager) != null)
                startActivity(browserIntent)
            else Toast.makeText(
                this,
                "Please, install a browser", Toast.LENGTH_LONG
            ).show()
        } else Toast.makeText(
            this,
            "The link is invalid", Toast.LENGTH_LONG
        ).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            codeScanner.startPreview()
        } else {
            Toast.makeText(
                this,
                "Can not scan until you give the camera permission", Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        checkCameraPermission()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }
}
