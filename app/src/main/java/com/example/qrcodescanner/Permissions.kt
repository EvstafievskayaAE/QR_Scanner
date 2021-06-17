package com.example.qrcodescanner

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

const val REQUEST_CODE = 200
const val CAMERA_PERMISSION = Manifest.permission.CAMERA

fun Activity.checkPermission(permission: String): Boolean {
    return if (ContextCompat.checkSelfPermission(
            this,
            permission
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA), REQUEST_CODE
        )
        false
    } else true
}
