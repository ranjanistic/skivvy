package org.ranjanistic.skivvy

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.net.wifi.p2p.WifiP2pDevice.CONNECTED
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.ranjanistic.skivvy.manager.SystemFeatureManager
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


class SkivvyConnect : AppCompatActivity() {
    val REQUEST_ENABLE_BT = 1
    var lv_paired_devices: ListView? = null
    var set_pairedDevices: Set<BluetoothDevice>? = null
    var adapter_paired_devices: ArrayAdapter<String>? = null
    var bluetoothAdapter: BluetoothAdapter? = null
    val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    val MESSAGE_READ = 0
    val MESSAGE_WRITE = 1
    val CONNECTING = 2
    val CONNECTED = 3
    val NO_SOCKET_FOUND = 4
    var bluetooth_message = "00"
    lateinit var skivvy:Skivvy
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        skivvy = this.application as Skivvy
        setTheme(skivvy.getThemeState())
        setContentView(R.layout.activity_skivvy_connect)
        initialize_layout()
        initialize_bluetooth()
        start_accepting_connection()
        initialize_clicks()

    }
    @SuppressLint("HandlerLeak")
     var mHandler: Handler = object : Handler() {
        override fun handleMessage(msg_type: Message) {
            super.handleMessage(msg_type)
            if(feature.isBluetoothOn()) {
                when (msg_type.what) {
                    MESSAGE_READ -> {
                        val readbuf = msg_type.obj as ByteArray
                        Toast.makeText(
                            applicationContext,
                            String(readbuf),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    MESSAGE_WRITE -> if (msg_type.obj != null) {
                        val connectedThread = ConnectedThread(msg_type.obj as BluetoothSocket)
                        connectedThread.write(bluetooth_message.toByteArray())
                    }
                    CONNECTED -> Toast.makeText(
                        applicationContext,
                        "Connected",
                        Toast.LENGTH_SHORT
                    ).show()
                    CONNECTING -> Toast.makeText(
                        applicationContext,
                        "Connecting...",
                        Toast.LENGTH_SHORT
                    ).show()
                    NO_SOCKET_FOUND -> Toast.makeText(
                        applicationContext,
                        "No socket found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                feature.bluetooth(true)
                Toast.makeText(
                    applicationContext,
                    "Turning on",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun start_accepting_connection() {
        //call this on button click as suited by you
        val acceptThread =  AcceptThread();
        acceptThread.start()
        Toast.makeText(applicationContext,"accepting",Toast.LENGTH_SHORT).show();
    }
    fun initialize_clicks() {
        lv_paired_devices!!.onItemClickListener =
            OnItemClickListener { parent, view, position, id ->
                val objects: Array<Any> = set_pairedDevices!!.toTypedArray()
                val device = objects[position] as BluetoothDevice
                val connectThread = ConnectThread(device)
                connectThread.start()
                Toast.makeText(
                    applicationContext,
                    "device choosen " + device.name,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
    val feature = SystemFeatureManager()
    private fun initialize_layout() {
        lv_paired_devices = findViewById(R.id.lv_paired_devices)
        adapter_paired_devices = ArrayAdapter<String>(applicationContext,R.layout.support_simple_spinner_dropdown_item)
        lv_paired_devices!!.adapter = adapter_paired_devices;
    }
    fun initialize_bluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(applicationContext,"Your Device doesn't support bluetooth.",Toast.LENGTH_SHORT).show();
            //finish()
        }
        if (!feature.isBluetoothOn())
            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT)
        else {
            set_pairedDevices = bluetoothAdapter!!.bondedDevices
            if (set_pairedDevices!!.isNotEmpty()) {
                for (device in set_pairedDevices!!) {
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    adapter_paired_devices!!.add(deviceName)
                }
            }
        }
    }


 inner class AcceptThread : Thread() {
        private val serverSocket: BluetoothServerSocket?
        override fun run() {
            var socket: BluetoothSocket? = null
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                socket = try {
                    serverSocket!!.accept()
                } catch (e: IOException) {
                    break
                }

                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    mHandler.obtainMessage(CONNECTED).sendToTarget()
                }
            }
        }

        init {
            var tmp: BluetoothServerSocket? = null
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = bluetoothAdapter!!.listenUsingRfcommWithServiceRecord("NAME", MY_UUID)
            } catch (e: IOException) {
            }
            serverSocket = tmp
        }
    }

    inner class ConnectThread() : Thread() {
        private var mmSocket:BluetoothSocket? = null
        private var  mmDevice:BluetoothDevice? = null
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        constructor(device: BluetoothDevice) : this() {
            var tmp: BluetoothSocket? = null
            mmDevice = device;
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (e: IOException) {
            }
            mmSocket = tmp
        }
        override fun run() {
            // Cancel discovery because it will slow down the connection
            bluetoothAdapter!!.cancelDiscovery()

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mHandler.obtainMessage(CONNECTING).sendToTarget();
                mmSocket!!.connect()
            } catch (e:Exception) {
                // Unable to connect; close the socket and get out
            }

            // Do work to manage the connection (in a separate thread)
//            bluetooth_message = "Initial message"
//            mHandler.obtainMessage(MESSAGE_WRITE,mmSocket).sendToTarget();
        }

        /** Will cancel an in-progress connection, and close the socket */
        fun cancel() {
            try {
                mmSocket!!.close();
            } catch (e:IOException) { }
        }
    }
    inner class ConnectedThread(bluetoothSocket: BluetoothSocket) : Thread() {
        private var  mmSocket:BluetoothSocket? =  null
        private var mmInStream:InputStream? = null
        private var  mmOutStream:OutputStream? = null

        fun ConnectedThread(socket:BluetoothSocket) {
            mmSocket = socket;
            var tmpIn:InputStream? = null;
            var tmpOut:OutputStream? = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.inputStream;
                tmpOut = socket.outputStream;
            } catch (e:IOException) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        override fun run() {
            val buffer = ByteArray(2)  // buffer store for the stream
            var bytes = 0 // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream!!.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();

                } catch (e:IOException) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        fun write(bytes:ByteArray) {
            try {
                mmOutStream?.write(bytes)
            } catch (e:IOException) { }
        }

        /* Call this from the main activity to shutdown the connection */
        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e:IOException) { }
        }
    }
}
