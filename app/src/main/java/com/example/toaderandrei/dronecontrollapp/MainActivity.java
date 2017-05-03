package com.example.toaderandrei.dronecontrollapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    ArrayAdapter<String> listAdapter;
    BluetoothAdapter btAdapter;
    Set<BluetoothDevice> devicesArray;
    ArrayList<String> pairedDevices;
    ArrayList<BluetoothDevice> devices;
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    protected static final int SUCCESS_CONNECT = 0;
    protected static final int MESSAGE_READ = 1;
    IntentFilter filter;
    BroadcastReceiver receiver;
    String tag = "debugging";
    ConnectedThread connThread;
    public boolean pressedUp;
    private static final String MOVE_FORWARD = "F";
    private static final String MOVE_BACK = "B";
    private static final String MOVE_LEFT = "L";
    private static final String MOVE_RIGHT = "R";
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            Log.i(tag, "in handler");
            super.handleMessage(msg);
            switch(msg.what){
                case SUCCESS_CONNECT:
                    // DO something
                    ConnectedThread connectedThread = new ConnectedThread((BluetoothSocket)msg.obj);
                    Toast.makeText(getApplicationContext(), "CONNECT", 0).show();
                    String s = "successfully connected";
                    connectedThread.write(s);
                    Log.i(tag, "connected");
                    connThread = connectedThread;
                    break;

                case MESSAGE_READ:
                    byte[] readBuf = (byte[])msg.obj;
                    String string = new String(readBuf);
                    Toast.makeText(getApplicationContext(), string, 0).show();
                    break;
            }

        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button btnBT = (Button) findViewById (R.id.btnBT);
        final Button btnForward = (Button) findViewById (R.id.btnForward);
        final Button btnBack = (Button) findViewById (R.id.btnBack);
        final Button btnLeft = (Button) findViewById (R.id.btnLeft);
        final Button btnRight = (Button) findViewById (R.id.btnRight);
        init();
        if(btAdapter==null){
            Toast.makeText(getApplicationContext(), "No bluetooth detected", 0).show();
            finish();
        }
        else{
            if(!btAdapter.isEnabled()){
                turnOnBT();
            }

            getPairedDevices();
            startDiscovery();
        }

        btnBT.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View v) {


                                         AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this); //?
                                         builder.setTitle("Bluetooth Device");
                                         builder.setAdapter(listAdapter, new DialogInterface.OnClickListener() {
                                             @Override
                                             public void onClick(DialogInterface dialog, int which) {
                                                 // Do something with the selection
                                                 if(btAdapter.isDiscovering()){
                                                     btAdapter.cancelDiscovery();
                                                 }
                                                 if(listAdapter.getItem(which).contains("Paired")){
                                                     BluetoothDevice selectedDevice = devices.get(which);
                                                     ConnectThread connect = new ConnectThread(selectedDevice);
                                                     connect.start();
                                                     Log.i(tag, "in click listener");
                                                 }
                                                 else{
                                                     Toast.makeText(getApplicationContext(), "device is not paired", 0).show();
                                                 }
                                             }
                                         });

                                         AlertDialog alert = builder.create();
                                         alert.show();
                                     }
                                 }
                                 );

        btnForward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(connThread != null){
                    switch (event.getAction()) {

                        case MotionEvent.ACTION_DOWN:

                            if(pressedUp == false){
                                pressedUp = true;
                                new SendMoveChar(MOVE_FORWARD).execute();
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            if(pressedUp == true){
                                pressedUp = false;
                                new SendStandbyChar().execute();
                            }
                            break;
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Device is not paired", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        btnBack.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(connThread != null){
                    switch (event.getAction()) {

                        case MotionEvent.ACTION_DOWN:

                            if(pressedUp == false){
                                pressedUp = true;
                                new SendMoveChar(MOVE_BACK).execute();
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            if(pressedUp == true){
                                pressedUp = false;
                                new SendStandbyChar().execute();
                            }
                            break;
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Device is not paired", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        btnLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(connThread != null){
                    switch (event.getAction()) {

                        case MotionEvent.ACTION_DOWN:

                            if(pressedUp == false){
                                pressedUp = true;
                                new SendMoveChar(MOVE_LEFT).execute();
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            if(pressedUp == true){
                                pressedUp = false;
                                new SendStandbyChar().execute();
                            }
                            break;
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Device is not paired", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        btnRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(connThread != null){
                    switch (event.getAction()) {

                        case MotionEvent.ACTION_DOWN:

                            if(pressedUp == false){
                                pressedUp = true;
                                new SendMoveChar(MOVE_RIGHT).execute();
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            if(pressedUp == true){
                                pressedUp = false;
                                new SendStandbyChar().execute();
                            }
                            break;
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Device is not paired", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

    }



    private void startDiscovery() {
        // TODO Auto-generated method stub
        btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();

    }
    private void turnOnBT() {
        // TODO Auto-generated method stub
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, 1);
    }
    private void getPairedDevices() {
        // TODO Auto-generated method stub
        devicesArray = btAdapter.getBondedDevices();
        if(devicesArray.size()>0){
            for(BluetoothDevice device : devicesArray){
                pairedDevices.add(device.getName());

            }
        }
    }
    private void init() {

        listAdapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,0);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = new ArrayList<String>();
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        devices = new ArrayList<BluetoothDevice>();
        receiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if(BluetoothDevice.ACTION_FOUND.equals(action)){
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    devices.add(device);
                    String s = "";
                    for(int a = 0; a < pairedDevices.size(); a++){
                        if(device.getName().equals(pairedDevices.get(a))){
                            s = "(Paired)";
                            break;
                        }
                    }

                    listAdapter.add(device.getName()+" "+s+" "+"\n"+device.getAddress());
                } else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                    // run some code
                } else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    // run some code
                } else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                    if(btAdapter.getState() == btAdapter.STATE_OFF){
                        turnOnBT();
                    }
                }

            }
        };

        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, filter);
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED){
            Toast.makeText(getApplicationContext(), "Bluetooth must be enabled to continue", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private class ConnectThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
            Log.i(tag, "construct");
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.i(tag, "get socket failed");

            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            btAdapter.cancelDiscovery();
            Log.i(tag, "connect - run");
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
                Log.i(tag, "connect - succeeded");
            } catch (IOException connectException) {	Log.i(tag, "connect failed");
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {

            }
        }
    }

    class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;


        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer;  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    buffer = new byte[1024];
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();

                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String s) {
            try {
                mmOutStream.write(s.getBytes());
            } catch (IOException e) {

            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    class SendStandbyChar extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            while(!pressedUp) {
                sendStandbyChar();
            }
            return null;
        }
        private void sendStandbyChar(){
            if(connThread != null)
                connThread.write("S");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
    }
    class SendMoveChar extends AsyncTask<Void, Void, Void> {
        String s = "";
        public SendMoveChar(String s){
            this.s = s;
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            while(pressedUp) {
                sendMoveChar(s);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        private void sendMoveChar(String s){
            if(connThread != null)
                connThread.write(s);
        }
    }
}
