package br.com.mauker.blecm.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by mauker on 13/07/2016.
 */
public class DeviceAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<BluetoothDevice> items;

    public DeviceAdapter(Context context, ArrayList<BluetoothDevice> devices) {
        this.mContext = context;
        this.items = devices;
    }

    public void addDevice(@NonNull BluetoothDevice device) {
        items.add(device);
        notifyDataSetChanged();
    }

    public void removeDevice(@NonNull BluetoothDevice device) {
        if (items.contains(device)) {
            items.remove(device);
            notifyDataSetChanged();
        }
    }

    public void clearAll() {
        items.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        AdapterViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent, false);
            viewHolder = new AdapterViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (AdapterViewHolder) convertView.getTag();
        }

        BluetoothDevice device = items.get(i);

        viewHolder.tv_name.setText(device.getName() + " - " +  device.getAddress());

        return convertView;
    }

    private class AdapterViewHolder {
        protected TextView tv_name;

        public AdapterViewHolder(View item) {
            tv_name = (TextView) item.findViewById(android.R.id.text1);
        }
    }
}
