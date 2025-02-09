package ru.smartbandage.android.ui.devices

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import ru.smartbandage.android.R
import ru.smartbandage.android.ble.BandageCommandsWithNotification
import ru.smartbandage.android.ble.ConnectionManager
import ru.smartbandage.android.ble.ConnectionManager.parcelableExtraCompat
import ru.smartbandage.android.databinding.ActivityBandageDeviceBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BandageDeviceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBandageDeviceBinding
    private val device: BluetoothDevice by lazy {
        intent.parcelableExtraCompat(BluetoothDevice.EXTRA_DEVICE)
            ?: error("Missing BluetoothDevice from MainActivity!")
    }
    private val dateFormatter = SimpleDateFormat("MMM d, HH:mm:ss", Locale.US)
    private val characteristics by lazy {
        ConnectionManager.servicesOnDevice(device)?.flatMap { service ->
            service.characteristics ?: listOf()
        } ?: listOf()
    }

    private val notifyingCharacteristics = mutableListOf<UUID>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBandageDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val cmdRunner = BandageCommandsWithNotification.getInstance(device)
        cmdRunner.registerManager()

        cmdRunner.readTemperature {
            val editText = findViewById<EditText>(R.id.temperatureId)
            binding.temperatureId.setText(it)
            log("ReadTemperatue $it")
        }
        cmdRunner.readTemperature {
            binding.empidanceId.setText(it)
            log("ReadTemperatue $it")
        }
        cmdRunner.readTemperature {
            log("ReadTemperatue $it")
        }
    }

    override fun onDestroy() {
        BandageCommandsWithNotification.getInstance(device).unregisterListener()
        //ConnectionManager.unregisterListener(connectionEventListener)
        ConnectionManager.teardownConnection(device)
        super.onDestroy()
    }


    @SuppressLint("SetTextI18n")
    private fun log(message: String) {
        val formattedMessage = "${dateFormatter.format(Date())}: $message"
        runOnUiThread {
            val uiText = binding.logTextView.text
            val currentLogText = uiText.ifEmpty { "Beginning of log." }
            binding.logTextView.text = "$currentLogText\n$formattedMessage"
            binding.logScrollView.post { binding.logScrollView.fullScroll(View.FOCUS_DOWN) }
        }
    }
}
