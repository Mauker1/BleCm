package br.com.mauker.blecm.adapters;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import br.com.mauker.blecm.R;

/**
 * Created by mauke on 14/07/2016.
 */
public class CharacteristicAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<BluetoothGattCharacteristic> items;

    public CharacteristicAdapter(Context mContext, ArrayList<BluetoothGattCharacteristic> items) {
        this.mContext = mContext;
        this.items = items;
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.characteristic_item, parent, false);
            viewHolder = new AdapterViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (AdapterViewHolder) convertView.getTag();
        }

        BluetoothGattCharacteristic device = items.get(i);

        viewHolder.tv_name.setText("");

        return convertView;
    }

    private class AdapterViewHolder {
        protected TextView tv_name;
        protected TextView tv_value;


        public AdapterViewHolder(View item) {
            tv_name = (TextView) item.findViewById(R.id.characteristic_item_tv_name);
            tv_value = (TextView) item.findViewById(R.id.characteristic_item_tv_value);
        }
    }
}
