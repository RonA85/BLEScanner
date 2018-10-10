package com.example.mac.bleapps;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder>{

    private List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
    private List<String> actionsList = new ArrayList<>();
    private OnDeviceListener listener;

    public DeviceAdapter(OnDeviceListener listener) {
        this.listener = listener;
    }

    public interface OnDeviceListener{
        void onDeviceClick(String mDeviceAddress);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final BluetoothDevice device = bluetoothDeviceList.get(holder.getAdapterPosition());
        holder.tvDeviceName.setText(device.getName());
        holder.tvDeviceAddress.setText(device.getAddress());
        switch (actionsList.get(holder.getAdapterPosition())){
            //Device found
            case BluetoothDevice.ACTION_FOUND:
                holder.tvDeviceStatus.setText("Device found");
                break;
            //Device is now connected
            case BluetoothDevice.ACTION_ACL_CONNECTED:
                holder.tvDeviceStatus.setText("Device is now connected");
                break;
            //Device has disconnected
            case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                holder.tvDeviceStatus.setText("Device has disconnected");
                break;


        }


        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onDeviceClick(device.getAddress());
            }
        });
    }

    public void addDevice(BluetoothDevice device,String action){
        bluetoothDeviceList.add(device);
        actionsList.add(action);
        notifyDataSetChanged();
    }

    public void clear(){
        bluetoothDeviceList.clear();
        actionsList.clear();
    }

    @Override
    public int getItemCount() {
        return bluetoothDeviceList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.layout_device)
        LinearLayout layout;
        @BindView(R.id.tv_device_name)
        TextView tvDeviceName;
        @BindView(R.id.tv_device_address)
        TextView tvDeviceAddress;
        @BindView(R.id.tv_device_status)
        TextView tvDeviceStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
