package com.example.gyromouse

import androidx.lifecycle.ViewModel
import java.io.BufferedWriter
import java.net.Socket

class MainViewModel : ViewModel() {
    var socket: Socket? = null
    var writer: BufferedWriter? = null
    var x = 0
    var y = 0
    var dx = 0
    var dy = 0
    var event = ""


    fun sendData() {
        while (socket!!.isConnected) {

            writer!!.write("$event $dx $dy")
            writer!!.newLine()
            writer!!.flush()

            Thread.sleep(3)
        }
    }

    fun sendLeftClick() {
        Thread {
            writer!!.write("leftClick $dx $dy")
            writer!!.newLine()
            writer!!.flush()
            Thread.currentThread().join()
        }.start()
    }

    fun sendRightClick() {
        Thread {
            writer!!.write("rightClick $dx $dy")
            writer!!.newLine()
            writer!!.flush()
            Thread.currentThread().join()
        }.start()
    }
}