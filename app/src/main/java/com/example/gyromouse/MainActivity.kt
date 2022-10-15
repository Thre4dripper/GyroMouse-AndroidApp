package com.example.gyromouse

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.gyromouse.databinding.ActivityMainBinding
import com.example.gyromouse.databinding.IpDialogLayoutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.Socket
import kotlin.math.atan2

class MainActivity : AppCompatActivity(), SensorEventListener {
    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var socketThread: Thread

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var lastAccelerometer = FloatArray(3)
    private var lastMagnetometer = FloatArray(3)
    private var lastAccelerometerSet = false
    private var lastMagnetometerSet = false
    private var rotationMatrix = FloatArray(9)
    private var orientation = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        getIp()
    }

    private fun getIp() {
        //custom dialog layout
        val dialogBinding = DataBindingUtil.inflate<IpDialogLayoutBinding>(
            LayoutInflater.from(this), R.layout.ip_dialog_layout, null, false
        )
        dialogBinding.ipEditText.setText(viewModel.ip)
        // Create an alert dialog to get ip from user
        MaterialAlertDialogBuilder(this).setTitle("Enter IP").setView(dialogBinding.root)
            .setPositiveButton("Connect") { _, _ ->
                val ip = dialogBinding.ipEditText.text.toString()
                viewModel.ip = ip

                //now start socket connection
                connectToServer(ip)

            }.setNegativeButton("Exit") { _, _ ->
                finish()
            }.setCancelable(false).show()
    }

    private fun connectToServer(ip: String) {
        socketThread = Thread {
            try {
                viewModel.socket = Socket(ip, 8080)
                viewModel.socket!!.tcpNoDelay = true
                runOnUiThread {
                    Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show()
                }
                viewModel.writer =
                    BufferedWriter(OutputStreamWriter(viewModel.socket!!.getOutputStream()))

                viewModel.event = "move"
                viewModel.sendData()
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Error Connecting to Server", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                e.printStackTrace()
            }
        }
        socketThread.start()
    }

    override fun onStop() {
        super.onStop()
        viewModel.socket!!.close()
        socketThread.join()
    }

    override fun onResume() {
        super.onResume()
        lastAccelerometerSet = false
        lastMagnetometerSet = false
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        if (p0!!.sensor == accelerometer) {
            System.arraycopy(p0.values, 0, lastAccelerometer, 0, p0.values.size)
            lastAccelerometerSet = true
        } else if (p0.sensor == magnetometer) {
            System.arraycopy(p0.values, 0, lastMagnetometer, 0, p0.values.size)
            lastMagnetometerSet = true
        }

        if (lastAccelerometerSet && lastMagnetometerSet) {
            SensorManager.getRotationMatrix(
                rotationMatrix, null, lastAccelerometer, lastMagnetometer
            )
            SensorManager.getOrientation(rotationMatrix, orientation)

            val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
            val roll = Math.toDegrees(orientation[2].toDouble()).toFloat()

            if (!viewModel.calibrate) {
                viewModel.initAzimuth = azimuth
                viewModel.initPitch = pitch
                viewModel.calibrate = true
            }


            viewModel.azimuth = azimuth - viewModel.initAzimuth
            viewModel.pitch = pitch - viewModel.initPitch

            viewModel.dx = (viewModel.azimuth).toInt()/6
            viewModel.dy = (viewModel.pitch).toInt()/6

            binding.textView.text = "dx: ${viewModel.dx} dy: ${viewModel.dy}"
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }
}

