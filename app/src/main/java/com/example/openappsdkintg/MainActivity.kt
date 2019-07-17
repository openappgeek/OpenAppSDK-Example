package com.example.openappsdkintg

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import co.openapp.sdk.OALockCallBack
import co.openapp.sdk.OpenAppClient
import co.openapp.sdk.OpenAppService
import co.openapp.sdk.data.api.Status
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    val TAG = MainActivity::class.java.simpleName

    companion object {
        const val COMPANY_ID = ""
        const val USER_TOKEN = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // fetch the lock from api and save it in local db
        OpenAppService.getStagingService(application).fetchOALocks(COMPANY_ID, USER_TOKEN)

        OpenAppService.getStagingService(application).loadOALocks().observe(this, Observer { response ->
            if (response != null) {
                when (response.status) {
                    Status.SUCCESS -> {
                        Log.e(TAG, "SUCCESS ${response.data?.message} - ${response.data}")
                        start_scan.isEnabled = true
                        stop_scan.isEnabled = true
                    }
                    Status.ERROR -> Log.e(TAG, "ERROR ${response.message} ${response.data?.message}")
                    Status.LOADING -> Log.e(TAG, "Loading")
                }
            }
        })

        startScanning()
        stopScanning()
    }

    private fun startScanning() {
        start_scan.setOnClickListener {
            OpenAppService.getStagingService(application).startScan()
        }
        // scan the lock which user has access
        OpenAppService.getStagingService(application).getOALocks(object : OALockCallBack {
            override fun onLockState(state: OpenAppClient.State?) {
                Log.e(TAG, "Lock State - ${state?.name}")
            }

            override fun onError(errorMessage: String?) {
                Log.e(TAG, "Lock Error Message - $errorMessage")
            }

        }).observe(this, Observer { lock ->
            if (lock != null && lock.macAddress != null) {
                Log.e(TAG, "Lock - ${lock.macAddress} - ${lock.lockName} - ${lock.aesKey}")
                OpenAppService.getStagingService(application).startOperateLock(this, lock.macAddress)
            }
        })

    }

    private fun stopScanning() {
        stop_scan.setOnClickListener { OpenAppService.getStagingService(application).stopScan() }
    }
}
