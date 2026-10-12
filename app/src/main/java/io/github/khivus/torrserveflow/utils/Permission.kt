package io.github.khivus.torrserveflow.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import io.github.khivus.torrserveflow.R
import io.github.khivus.torrserveflow.atv.Utils.isChangHong
import kotlin.concurrent.thread


object Permission {
    fun requestPermissionWithRationale(activity: Activity) {
        if (!isChangHong) // TODO
            thread {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Snackbar.make(activity.findViewById(android.R.id.content) ?: return@thread, R.string.permission_storage_msg, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.permission_btn) {
                            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                        }
                        .show()
                } else {
                    ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                }
            }
    }

    fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }
}