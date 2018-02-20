package com.example.shoza.androidble;

import android.content.Context;
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

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView mDeviceListview;
    private Button testAdd;

    private static final String[] texts = {"device A", "device B", "device C", "device D"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDeviceListview = (ListView) findViewById(R.id.deviceListView);
        mDeviceListview.setOnItemClickListener(this);

        //final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, texts);
        //arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, texts);
        BaseAdapter adapter = new TestAdapter(this.getApplicationContext(), R.layout.listitem_device, texts, texts);
        mDeviceListview.setAdapter(adapter);

        testAdd = (Button) findViewById(R.id.testAdd);
        testAdd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.i("test", "testadd");
                //arrayAdapter.add("device Test");
            }
        });

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        Log.i("test", "list");

    }

    private class TestAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private int layoutID;
        private String[] namelist;
        private String[] addresslist;

        class ViewHolder{
            TextView text;
            TextView address;
        }


        TestAdapter(Context context, int itemLayoutId, String[] names, String[] addresses ){
            inflater = LayoutInflater.from(context);
            layoutID = itemLayoutId;
            namelist = names;
            addresslist = addresses;
        }

        @Override
        public int getCount(){
            return namelist.length;
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
               convertView = inflater.inflate(layoutID, null);
               holder = new ViewHolder();
               holder.text = convertView.findViewById(R.id.device_name);
               holder.address = convertView.findViewById(R.id.device_address);
               convertView.setTag(holder);
           }else{
               holder = (ViewHolder) convertView.getTag();
           }

           holder.text.setText(namelist[position]);
           holder.address.setText(addresslist[position]);
           return convertView;
        }
    }
}

