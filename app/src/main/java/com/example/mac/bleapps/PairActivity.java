package com.example.mac.bleapps;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;
import java.util.TreeMap;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.provider.Settings.NameValueTable.NAME;

public class PairActivity extends AppCompatActivity implements DeviceAdapter.OnDeviceListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv_devices)
    RecyclerView rvDevices;

    public static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MASSAGE_RECEIVED = 5;

    private static final UUID MY_UUID = UUID.fromString("69027635-0ef7-42d2-96d9-60a67ea863a0");

    private BluetoothAdapter mBluetoothAdapter;
    private DeviceAdapter mDeviceAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothLeService mBluetoothLeService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        rvDevices.setLayoutManager(new LinearLayoutManager(this));
        mDeviceAdapter = new DeviceAdapter(this);
        rvDevices.setAdapter(mDeviceAdapter);
        // Initializes Bluetooth adapter.
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
//        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
//        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
//        unbindService(mServiceConnection);
//        mBluetoothLeService = null;

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnClick({R.id.fab})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                } else {
                    // Register for broadcasts when a device is discovered.
                    mDeviceAdapter.clear();
                    mBluetoothAdapter.startDiscovery();
                    ServerClass serverClass = new ServerClass();
                    serverClass.start();
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mReceiver, filter);
                }
                break;
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceAdapter.addDevice(device, action);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Thank you for turning on Bluetooth", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Please turn on Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //scanLeDevice(true);
                    // Permission granted, yay! Start the Bluetooth device scan.
                } else {
                    Toast.makeText(this, "Please give a permission", Toast.LENGTH_SHORT).show();
                    // Alert the user that this application requires the location permission to perform the scan.
                }
            }
        }
    }

    @Override
    public void onDeviceClick( BluetoothDevice device) {
        mBluetoothAdapter.cancelDiscovery();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            device.createBond();
        }
        ClientClass clientClass = new ClientClass(device);
        clientClass.start();
//        Toast.makeText(this,mDeviceAddress,Toast.LENGTH_SHORT).show();
//        mBluetoothLeService.connect(mDeviceAddress);
    }

//    Handler handler = new Handler(new Handler.Callback() {
//        @Override
//        public boolean handleMessage(Message message) {
//            switch (message.what){
//                case STATE_LISTENING:
//
//                    break;
//
//            }
//        }
//    });

    private class ServerClass extends Thread {
        private BluetoothServerSocket serverSocket;
        public ServerClass(){
            try {
                serverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(getResources().getString(R.string.app_name),MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            BluetoothSocket socket = null;

            while (socket==null){
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;

                    socket= serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                }

                if(socket!=null){
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    break;
                }
            }
        }
    }

    private class ClientClass extends Thread{
        private BluetoothDevice mDevice;
        private BluetoothSocket mSocket;

        public ClientClass (BluetoothDevice device){
            this.mDevice = device;

            try {
                mSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                mSocket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;

            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
            }
        }
    }


}
