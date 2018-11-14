package fr.dimtion.tunnel

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_RECORD_AUDIO = 1
    private val TAG = "MainActivity"

    lateinit var samplingLoop: SamplingLoop

    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i(TAG, "Asking permissions")
        handlePermissions()

        textView = findViewById(R.id.freq)
    }

    override fun onResume() {
        Log.i(TAG, "Resuming Activity")
        super.onResume()
    }

    private fun handlePermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.RECORD_AUDIO
                )
            ) {
                Log.w(TAG, "We should show a rationale for the RECORD_AUDIO permission.")

            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    PERMISSIONS_REQUEST_RECORD_AUDIO
                )
            }
        } else {
            // Permission has already been granted
            // TODO: start recording
            Log.i(TAG, "We already have permission, proceeding...")
            start_recording()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_RECORD_AUDIO -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.d(TAG, "Permission granted, proceeding...")
                    start_recording()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    // TODO: print message saying that we need the permission
                    Log.w(TAG, "RECORD_AUDIO permission denied.")
                    Log.w(TAG, grantResults[0].toString())
                }
                return
            }
            else -> {
                Log.e(TAG, "Unhandled request Code: $requestCode")
            }
        }
    }

    fun start_recording() {
        samplingLoop = SamplingLoop(this)
        samplingLoop.start()
    }

    fun updateTextView(text: String) {
        textView.text = text
    }
}
