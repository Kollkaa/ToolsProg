package com.tools.prog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;


public class BluetoothConectActivity extends Activity {
    private ProgressDialog mProgressDlg;
    private static final int REQUEST_ENABLE_BT = 1;
    private Button onBtn;
    private Button offBtn;
    Set<BluetoothDevice> pairedDevices;
    private TextView discover;
    private BluetoothAdapter myBluetoothAdapter;
    private TreeMap<String, String> dev;
    private ListView myListView;
    ArrayList<String> mListDevice;
    /*    private ArrayAdapter<String> BTArrayAdapter;*/
    BluetoothHeadset bluetoothHeadset;

    // Get the default adapter

    private ArrayList<String> list = new ArrayList<String>();
    TreeMap<String, String> arr;
    private ArrayList<BluetoothDevice> mDeviceList;
    IntentFilter filter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_conect);
        dev = new TreeMap<>();
        discover = (TextView) findViewById(R.id.discover);
        onBtn = (Button) findViewById(R.id.onBtn);
        offBtn = (Button) findViewById(R.id.offBtn);

        myListView = (ListView) findViewById(R.id.dynamic1);
        mDeviceList = new ArrayList<>();
        mListDevice = new ArrayList<>();
        arr = new TreeMap<>();
        mProgressDlg = new ProgressDialog(this);
        mProgressDlg.setMessage("Scanning...");
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mProgressDlg.setCancelable(false);
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                myBluetoothAdapter.cancelDiscovery();
            }
        });

        if (myBluetoothAdapter == null) {

            showUnsupported();

        } else {

            if (myBluetoothAdapter.isEnabled()) {
                showEnabled();
            } else {
                showDisabled();
            }
        }
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        filter  = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);


        registerReceiver(mReceiver, filter);

    }



    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toPlayer:
                Intent intent1 = new Intent(this, MainActivity.class);
                intent1.putExtra("bluetooth", myBluetoothAdapter.isEnabled());
                startActivity(intent1);
                break;
            case R.id.listBtn:
                pairedDevices = myBluetoothAdapter.getBondedDevices();

                if (pairedDevices == null || pairedDevices.size() == 0) {
                    showToast("No Paired Devices Found");
                } else {
                    ArrayList<String> list = new ArrayList<String>();
                    Iterator<BluetoothDevice> iter = pairedDevices.iterator();
                    while (iter.hasNext()) {
                        BluetoothDevice a = iter.next();
                        String name = a.getName();
                        String adress = a.getAddress();
                        int d = a.getBondState();

                        try {
                            showToast(name + "-" + adress);
                            list.add(name + "-" + adress);
                            arr.put(name, adress);
                        } catch (Exception e) {
                        }

                    }

                    try {
                        myListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                                android.R.layout.simple_list_item_multiple_choice, list);
                        showToast("3");
                        myListView.setAdapter(adapter);

                    } catch (Exception e) {
                    }


                }


                break;
            case R.id.button:
                Log.d("dsd", "checked: ");
                showToast("checked: ");

                SparseBooleanArray sbArray = myListView.getCheckedItemPositions();
                for (int i = 0; i < sbArray.size(); i++) {
                    int key = sbArray.keyAt(i);
                    if (sbArray.get(key)) {
                        mListDevice.add(list.get(key));
                        showToast(list.get(key));
                    }
                }


                filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
                filter.addAction(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED);
                registerReceiver(mReceiver, filter);

                myBluetoothAdapter.getProfileProxy(this, mA2dpListener , BluetoothProfile.A2DP);


                break;
            case R.id.seached:
                if (myBluetoothAdapter.isDiscovering()) {
                    // the button is pressed when it discovers, so cancel the discovery
                    myBluetoothAdapter.cancelDiscovery();
                } else {
                    // BTArrayAdapter.clear();
                    myBluetoothAdapter.startDiscovery();

                    registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
                }
                break;
            case R.id.onBtn:
                Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

                break;
            case R.id.offBtn:
                myBluetoothAdapter.disable();

                showDisabled();
                break;
        }
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BluetoothProfile.ServiceListener profileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = (BluetoothHeadset) proxy;
            }
        }

        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = null;
            }
        }
    };

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onPause() {
        if (myBluetoothAdapter != null) {
            if (myBluetoothAdapter.isDiscovering()) {
                myBluetoothAdapter.cancelDiscovery();
            }
        }


        super.onPause();
    }

    @Override
    public void onDestroy() {


        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void showEnabled() {
        discover.setText("Bluetooth is On");
        discover.setTextColor(Color.BLUE);
        offBtn.setEnabled(true);
        onBtn.setEnabled(false);

    }

    private void showDisabled() {
        discover.setText("Bluetooth is Off");
        discover.setTextColor(Color.RED);
        offBtn.setEnabled(false);
        onBtn.setEnabled(true);

    }

    private void showUnsupported() {
        discover.setText("Bluetooth is unsupported by this device");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (requestCode == REQUEST_ENABLE_BT) {
            if (myBluetoothAdapter.isEnabled()) {
                showEnabled();
            } else {
                discover.setText("Status: Disabled");
            }
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON) {
                    showToast("Enabled");

                    showEnabled();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mDeviceList = new ArrayList<BluetoothDevice>();

                mProgressDlg.show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mProgressDlg.dismiss();


            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                mDeviceList.add(device);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                dev.put(deviceName, deviceHardwareAddress);

            }
            if (action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_DISCONNECTED);
                if (state == BluetoothA2dp.STATE_CONNECTED) {
                    setIsA2dpReady(true);
                   startAct();
                } else if (state == BluetoothA2dp.STATE_DISCONNECTED) {
                    setIsA2dpReady(false);
                }
            } else if (action.equals(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_NOT_PLAYING);
                if (state == BluetoothA2dp.STATE_PLAYING) {
                    Log.d("212", "A2DP start playing");
                   showToast("A2dp is playing");
                } else {
                    Log.d("212", "A2DP stop playing");
                    showToast("A2dp is stopped");
                }
            }
        }
    };


// Establish connection to the proxy.

    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    boolean mIsA2dpReady = false;

    void setIsA2dpReady(boolean ready) {
        mIsA2dpReady = ready;
        Toast.makeText(this, "A2DP ready ? " + (ready ? "true" : "false"), Toast.LENGTH_SHORT).show();
    }

    AudioManager mAudioManager;

    BluetoothA2dp mA2dpService;

    private BluetoothProfile.ServiceListener mA2dpListener = new BluetoothProfile.ServiceListener() {

        @Override
        public void onServiceConnected(int profile, BluetoothProfile a2dp) {
            Log.d("212", "a2dp service connected. profile = " + profile);
            if (profile == BluetoothProfile.A2DP) {
                mA2dpService = (BluetoothA2dp) a2dp;
                if (mAudioManager.isBluetoothA2dpOn()) {
                    setIsA2dpReady(true);
                    startAct();
                } else {
                    Log.d("212", "bluetooth a2dp is not on while service connected");
                }
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            setIsA2dpReady(false);
        }

    };

    public void startAct() {
        startActivity(new Intent(this, MainActivity.class));
    }
}

