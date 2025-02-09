package ru.smartbandage.android.ble

import android.bluetooth.BluetoothDevice
import ru.smartbandage.android.ui.devices.decodeHex
import ru.smartbandage.android.ui.devices.toHex
import timber.log.Timber
import java.util.concurrent.ConcurrentLinkedQueue

class BandageCommandsWithNotification(
    val device: BluetoothDevice
){
    private val operationQueue = ConcurrentLinkedQueue<CommandWithNotification>()
    private var pendingOperation: CommandWithNotification? = null

    private val characteristics by lazy {
        ConnectionManager.servicesOnDevice(device)?.flatMap { service ->
            service.characteristics ?: listOf()
        } ?: listOf()
    }

    val characteristic by lazy {
        characteristics.first { it.isNotifiable() && it.isReadable() && it.isWritable() }
    }


    fun registerManager() {
        ConnectionManager.registerListener(connectionEventListener)
        ConnectionManager.enableNotifications(device, characteristic)
    }
    fun unregisterListener() {
        ConnectionManager.unregisterListener(connectionEventListener)
    }

    private val connectionEventListener by lazy {
        ConnectionEventListener().apply {


            onCharacteristicChanged = { _, characteristic, value ->
                pendingOperation?.onSuccess?.let { it(value.toHex()) }
                pendingOperation = null
                doNextOperation()
//                log("Value changed on ${characteristic.uuid}: ${value.toHexString()}")
            }

/*
            onNotificationsEnabled = { _, characteristic ->
//                log("Enabled notifications on ${characteristic.uuid}")
//                notifyingCharacteristics.add(characteristic.uuid)
            }
*/
        }
    }

    @Synchronized
    private fun enqueueOperation(operation: CommandWithNotification) {
        operationQueue.add(operation)
        if (pendingOperation == null) {
            doNextOperation()
        }
    }

    @Synchronized
    private fun doNextOperation() {
        if (pendingOperation != null) {
            Timber.e("doNextOperation() called when an operation is pending! Aborting.")
            return
        }

        val operation = operationQueue.poll() ?: run {
            Timber.v("Operation queue empty, returning")
            return
        }
        pendingOperation = operation
        ConnectionManager.writeCharacteristic(device, characteristic, operation.operationCommand.decodeHex())

    }

    public fun readTemperature(onSuccess : (String)-> Unit){
        enqueueOperation(CommandWithNotification("58", onSuccess))
    }


    private data class CommandWithNotification(
        val operationCommand : String,
        val onSuccess : (String)-> Unit
    )

    companion object {
        @Volatile private var INSTANCE: BandageCommandsWithNotification? = null

        fun getInstance(device: BluetoothDevice): BandageCommandsWithNotification =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildObject(device).also {
                    it.registerManager()
                    INSTANCE = it
                }
            }

        private fun buildObject(device: BluetoothDevice) : BandageCommandsWithNotification {
            return BandageCommandsWithNotification(device = device)
        }
    }
}
