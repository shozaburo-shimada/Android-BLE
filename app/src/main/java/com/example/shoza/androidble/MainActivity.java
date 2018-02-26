package com.example.shoza.androidble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int RC_HANDLE_PERMISSION = 1;
    private static final long SCAN_PERIOD = 10000;

    //private ListView mDeviceListview;
    //private TestAdapter adapter;
    private ListView mDeviceListview;
    private LeDeviceListAdapter mLeDeviceListAdapter;

    private Button testAdd;
    private static final String[] texts = {"device A", "device B", "device C", "device D"};
    private List<String> items;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        items = new ArrayList<>(Arrays.asList(texts));

        mDeviceListview = (ListView) findViewById(R.id.deviceListView);
        mDeviceListview.setOnItemClickListener(this);

        /*
        adapter = new TestAdapter(this, R.layout.listitem_device, items, items);
        mDeviceListview.setAdapter(adapter);
        */

        testAdd = (Button) findViewById(R.id.testAdd);
        testAdd.setOnClickListener(this);

        // BLEが使用可能かチェック
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // BluetoothAdapter初期化。API level 18 必要
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.i(TAG, "onResume");


        // Bluetooth機能が有効になっているかのチェック。無効の場合はダイアログを表示して有効をうながす。
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                return;
            }
        }

        // パーミッションたずねてしまおう
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkPermission()) {
            Log.i(TAG, "not granted yet");
            // ダイアログを出してパーミッション許可を取りに行く
            String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permissions, RC_HANDLE_PERMISSION);
        }
        // 既にパーミッションを得ている or M未満だから関係ない場合
        else {
            Log.i(TAG, "already granted");
            doInitialize();
        }

    }

    /**
     * パーミッションを確認する
     * @return true if yet granted, false if not granted
     */
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * パーミッションを尋ねた結果
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionsResult");

        // パーミッション許可してるか
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "許可してよ", Toast.LENGTH_LONG).show();
                finish();
            }
        }
        doInitialize();
    }

    /**
     * 初期化処理を行う
     */
    private void doInitialize() {

        // BluetoothLeScanner用意
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mBluetoothLeScanner == null) {
            Toast.makeText(this, "あれ", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        mDeviceListview.setAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }

    private void scanLeDevice(final boolean enable) {
        if (mBluetoothLeScanner == null) {
            return;
        }
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothLeScanner.stopScan(mScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothLeScanner.startScan(mScanCallback);
        } else {
            mScanning = false;
            mBluetoothLeScanner.stopScan(mScanCallback);
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        Log.i(TAG, "list");

    }

    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.testAdd:{
                Log.i(TAG, "testadd");
                //items.add("test");
                //adapter.notifyDataSetChanged();
            }

        }

    }


    /**
     * ListViewのAdapter
     */
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup){

            static class ViewHolder {
                TextView deviceName;
                TextView deviceAddress;
            }

            ViewHolder viewHolder;

            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

/*
    private class TestAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private int layoutID;
        //private String[] namelist;
        //private String[] addresslist;
        private List<String> namelist;
        private List<String> addresslist;

        class ViewHolder{
            TextView text;
            TextView address;
        }


        TestAdapter(Context context, int itemLayoutId, List<String> names, List<String> addresses ){
            super();
            this.inflater = LayoutInflater.from(context);
            this.layoutID = itemLayoutId;
            this.namelist = names;
            this.addresslist = addresses;
        }

        public void addDevice(String name){
            namelist.add(name);
            addresslist.add(name);
        }

        @Override
        public int getCount(){
            //return namelist.length;
            return namelist.size();
        }

        @Override
        public Object getItem(int position){
            return position;
        }

        @Override
        public long getItemId(int position){
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){

            ViewHolder holder;

           if(convertView == null){
               //convertView = inflater.inflate(layoutID, parent, false);
               convertView = inflater.inflate(layoutID, null);
               holder = new ViewHolder();
               holder.text = convertView.findViewById(R.id.device_name);
               holder.address = convertView.findViewById(R.id.device_address);
               convertView.setTag(holder);
           }else{
               holder = (ViewHolder) convertView.getTag();
           }

           holder.text.setText(namelist.get(position));
           holder.address.setText(addresslist.get(position));

           return convertView;

        }

    }
*/

}


