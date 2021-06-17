package com.example.qrcodescanner

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.example.qrcodescanner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    private lateinit var codeScanner: CodeScanner
    private lateinit var reverseCameraImage: ImageView
    private lateinit var cancelTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        reverseCameraImage = binding.reverseCameraImageView
        cancelTextView = binding.cancelTextView

        val factory = MainViewModelFactory()
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        initCodeScanner()

        codeScanner.decodeCallback = DecodeCallback { result ->
            runOnUiThread {
                viewModel.updateUrl(result.text)
                Toast.makeText(
                    this,
                    "Scan result: ${result.text}", Toast.LENGTH_LONG
                ).show()
                openUrlInBrowser()
            }
        }

        codeScanner.errorCallback = ErrorCallback { exception ->
            runOnUiThread {
                Toast.makeText(
                    this,
                    "Camera error: ${exception.message}", Toast.LENGTH_LONG
                ).show()
            }
        }

        reverseCameraImage.setOnClickListener(this)
        cancelTextView.setOnClickListener(this)

        checkCameraPermission()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            reverseCameraImage.id -> {
                reverseCamera()
            }
            cancelTextView.id -> {
                codeScanner.startPreview()
            }
        }
    }

    private fun initCodeScanner() {
        codeScanner = CodeScanner(this, binding.scannerView)
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false
    }

    private fun reverseCamera() {
        if (codeScanner.camera == CodeScanner.CAMERA_BACK)
            codeScanner.camera = CodeScanner.CAMERA_FRONT
        else codeScanner.camera = CodeScanner.CAMERA_BACK
    }

    private fun checkCameraPermission() {
        if (this.checkPermission(CAMERA_PERMISSION))
            codeScanner.startPreview()
    }

    private fun openUrlInBrowser() {
        if (viewModel.isValidUrl()) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(viewModel.getUrl()))
            if (browserIntent.resolveActivity(packageManager) != null)
                startActivity(browserIntent)
            else Toast.makeText(
                this,
                "No app found to open the link", Toast.LENGTH_LONG
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
        if (requestCode == REQUEST_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) codeScanner.startPreview()
    }

    override fun onResume() {
        super.onResume()
        checkCameraPermission()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    override fun onDestroy() {
        codeScanner.stopPreview()
        super.onDestroy()
    }
}
