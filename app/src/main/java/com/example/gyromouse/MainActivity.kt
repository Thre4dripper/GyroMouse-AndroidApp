package com.example.gyromouse

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.gyromouse.databinding.ActivityMainBinding
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.Socket

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var socketThread: Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        connectToServer("192.168.0.102")
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
}

