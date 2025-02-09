package ru.smartbandage.android.ui.devices

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.TRANSPORT_LE
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.smartbandage.android.R


/**
 * Фрагмент по тображению настроек подключения к устройству
 */
class SettingsFragment : Fragment() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private var bluetoothGatt: BluetoothGatt? = null
    private val deviceList: MutableList<BluetoothDevice> = mutableListOf()

    private lateinit var recyclerView: RecyclerView
    private lateinit var debugLog: TextView;

    private val handler = Handler()

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000


    fun Context.bluetoothAdapter(): BluetoothAdapter? =
        (this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("MyTag", "Это отладочное сообщение")
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
//        connectedDeviceTextView = view.findViewById(R.id.connectedDeviceTextView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
//        recyclerView.adapter = DeviceAdapter(deviceList) { device ->
//            connectToDevice(device)
//        }

//        listView = view.findViewById(R.id.listView)
        debugLog = view.findViewById(R.id.debug_log)

        val bta = requireContext().bluetoothAdapter()
        if (bta == null || !bta.isEnabled) {
            Toast.makeText(context, "Bluetooth не поддерживается", Toast.LENGTH_SHORT).show()
            return view
        } else {
            bluetoothAdapter = bta
            bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
            checkPermissionsAndStartScan()
        }
        return view
    }

    private fun checkPermissionsAndStartScan() {
        if (!bluetoothAdapter.isEnabled) {
            debugLog.append("Bluetooth не поддерживается или отключен")
        } else {
            debugLog.append("Bluetooth включен и поддерживается")
        }

        if
                (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                ),
                1
            )
        } else {
            scanDevices()
        }
    }

    @SuppressLint("MissingPermission")
    private fun scanDevices() {
//        handler.postDelayed({
//            debugLog.append("postDelayed \n\r")
//            bluetoothAdapter.bluetoothLeScanner?.stopScan(scanCallback)
//        }, SCAN_PERIOD)

        deviceList.clear()
        recyclerView.adapter?.notifyDataSetChanged()

        bluetoothAdapter.bluetoothLeScanner?.startScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            debugLog.append("onScanResult\n\r")
            super.onScanResult(callbackType, result)
            val device = result.device

            if (!deviceList.contains(device)) {
                deviceList.add(device)
                //deviceNames.add(device.name + "\n" + device.address)
                //updateListView()
                recyclerView.adapter?.notifyItemInserted(deviceList.size - 1)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            debugLog.append("onScanFailed\n\r")
            super.onScanFailed(errorCode)
            Toast.makeText(
                context,
                "Сканирование не удалось с кодом ошибки: $errorCode",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onPause() {
        debugLog.append("onPause\n\r")
        super.onPause()
        bluetoothLeScanner.stopScan(scanCallback)
        bluetoothGatt?.close()

    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        bluetoothLeScanner.stopScan(scanCallback)
        debugLog.append("Try to connect ${device.name}\n\r")
        lifecycleScope.launch(Dispatchers.IO) {
            bluetoothGatt = device.connectGatt(
                activity, false,
                object : BluetoothGattCallback() {
                    override fun onConnectionStateChange(
                        gatt: BluetoothGatt,
                        status: Int,
                        newState: Int
                    ) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            debugLog.append("coroutine test\n\r ")
                            debugLog.append("coroutine Test Connected to name : ${device.name} \n\r")
                        }
                        //debugLog.append("123статус BluetoothGattCallback: $status новый статус $newState ${newState == BluetoothProfile.STATE_CONNECTED}\n\r ")
                        debugLog.append("test\n\r ")
                        debugLog.append("Test Connected to name : ${device.name} \n\r")
                        //super.onConnectionStateChange(gatt, status, newState)
                        /*
                                            when (newState) {
                                                BluetoothProfile.STATE_CONNECTED -> {
                                                    debugLog.append("Connected to name : ${device.name} \n\r")
                                                    Handler(Looper.getMainLooper()).postDelayed({
                                                        debugLog.append("Connected with delay to name : ${device.name} gatt: $gatt\n\r")
                                                        gatt.discoverServices()
                                                    }, 1000)
                                                }
                                                BluetoothProfile.STATE_DISCONNECTED -> {
                                                    debugLog.append("BluetoothProfile.STATE_DISCONNECTED\n\r")
                                                    gatt.close()
                                                }
                                                else -> {
                                                    debugLog.append("Error unknown state\n\r")
                                                }
                                            }
                        */
                    }
                    /*                override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                                        debugLog.append("onServicesDiscovered BlueTooth Le ready\n\r")
                                        super.onServicesDiscovered(gatt, status)
                                        if (status == BluetoothGatt.GATT_SUCCESS) {
                                            //enableNotifications(gatt)
                                            sendCommandToDevice(gatt)
                                        }
                                    }

                                    override fun onCharacteristicChanged(
                                        gatt: BluetoothGatt,
                                        characteristic: BluetoothGattCharacteristic,
                                        value: ByteArray
                                    ) {
                                        debugLog.append("Response ${value.toHex()}:\n\r")
                                        super.onCharacteristicChanged(gatt, characteristic, value)
                                    }

                                    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                                        super.onCharacteristicWrite(gatt, characteristic, status)
                                        if (status == BluetoothGatt.GATT_SUCCESS) {
                                            debugLog.append("Write command Success:\n\r")
                                        } else {
                                            debugLog.append("Write command Failed:\n\r")
                                        }
                                    }*/

                }, TRANSPORT_LE
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendCommandToDevice(gatt: BluetoothGatt?) {
        try {
            val services = gatt?.services
            debugLog.append("readServices:\n\r")
            services?.forEach {
                debugLog.append("service : ${it.uuid}\n\r")
            }
            val service = gatt?.getService(services?.first()?.uuid)
            debugLog.append("readCharacteristics:\n\r")
            service?.characteristics?.forEach {
                debugLog.append("${it.uuid}\n\r")
            }
            /*
                        val characteristic = service?.getCharacteristic(service.characteristics?.first()?.uuid)

                        characteristic?.let { char ->
                            // Устанавливаем значение характеристики (команда 0x58)
                            val ReadTemperatureCmd = "58".decodeHex()
                            char.value = ReadTemperatureCmd
                            gatt.writeCharacteristic(char)

                            // Включаем уведомления для характеристики
                            gatt.setCharacteristicNotification(char, true)

                            // Устанавливаем дескриптор для уведомлений
                            val descriptor =
                                char.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(descriptor)
                        }
            */
        } catch (e: Throwable) {
            debugLog.append("Error in sendCommandToDevice" + e.message)
        }
    }


/*    inner class DeviceAdapter(
        private val devices: List<BluetoothDevice>,
        private val onConnectClick: (BluetoothDevice) -> Unit
    ) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

        inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val deviceName: TextView = itemView.findViewById(R.id.deviceName)
            val connectButton: Button = itemView.findViewById(R.id.connectButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
            return DeviceViewHolder(view)
        }

        @SuppressLint("MissingPermission")
        override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
            val device = devices[position]
            holder.deviceName.text = device.name ?: device.address
            holder.connectButton.setOnClickListener {
                onConnectClick(device)
            }
        }

        override fun getItemCount(): Int {
            return devices.size
        }
    }*/


}