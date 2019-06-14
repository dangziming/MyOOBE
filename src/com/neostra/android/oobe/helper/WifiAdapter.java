package com.neostra.android.oobe.helper;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import com.neostra.android.oobe.R;

public class WifiAdapter extends BaseAdapter {

    private class ViewHolder {
        private TextView ssid_view;
        private ImageView connected_view;
        private ImageView lock_view;
    }

    private List<WifiAP> list;

    private OnClickItemListener onClickItemListener;

    public void setOnClickItemListener(OnClickItemListener onClickItemListener) {
        this.onClickItemListener = onClickItemListener;
    }

    public WifiAdapter(List<WifiAP> list) {
        this.list = list;
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = View.inflate(parent.getContext(), R.layout.wifi_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.ssid_view = (TextView) convertView.findViewById(R.id.ssid);
            viewHolder.connected_view = (ImageView) convertView.findViewById(R.id.connected);
            viewHolder.lock_view = (ImageView)convertView.findViewById(R.id.lock);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.ssid_view.setText(list.get(position).getSSID());
        // if (position == 0 && list.get(position).isConnected()) {
        //     viewHolder.ssid_view.setTextColor(parent.getContext().getResources().getColor(R.color.ssidConnectedColor));
        //     viewHolder.connected_view.setVisibility(View.VISIBLE);
        // } else {
        //     viewHolder.ssid_view.setTextColor(parent.getContext().getResources().getColor(R.color.ssidColor));
        //     viewHolder.connected_view.setVisibility(View.INVISIBLE);
        // }
        if (list.get(position).isLock()) {
            viewHolder.lock_view.setImageResource(R.drawable.ic_wifi_lock);
        } else {
            viewHolder.lock_view.setImageResource(R.drawable.ic_wifi);
        }
        
        return convertView;
    }

    public interface OnClickItemListener{
        void onClickItemListener(int position);
    }
}
