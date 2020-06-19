package kr.co.signallink.svsv2.views.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.List;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.commons.DefFile;
import kr.co.signallink.svsv2.user.RegisterSVSItem;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.views.activities.BetteryInfoGetterActivity;

public class RegisterSVSValueItemsAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<RegisterSVSItem> devices;
    private ArrayAdapter<DefFile.SVS_LOCATION> svsLocAdapter;
    private List<DefFile.SVS_LOCATION> svsLocations;

    public RegisterSVSValueItemsAdapter(Context context, List<RegisterSVSItem> devices, String pipePumpMode) {
        this.context = context;
        inflater = LayoutInflater.from(context);

        this.devices = devices;
        //svsLocations = DefFile.SVS_LOCATION.getArrayList(); // added by hslee
        if( "pump".equals(pipePumpMode) ) {
            svsLocations = DefFile.SVS_LOCATION.getArrayList(); // added by hslee
        }
        else {
            svsLocations = DefFile.SVS_LOCATION.getArrayListPipe(); // added by hslee   need to modify 파이프, 펌프 구분
        }
        svsLocAdapter = new ArrayAdapter<DefFile.SVS_LOCATION>(context, R.layout.spinner_item, svsLocations);
        svsLocAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewGroup vg;
        if (convertView != null) {
            vg = (ViewGroup) convertView;
        } else {
            vg = (ViewGroup) inflater.inflate(R.layout.list_svs_element, null);
        }

        final RegisterSVSItem device = devices.get(position);

        final ImageButton cbSvs = vg.findViewById(R.id.select_svsdevice);
        final TextView tvName = vg.findViewById(R.id.select_svsname);
        final TextView tvAdd = vg.findViewById(R.id.select_svsaddress);
        final TextView tvRSSI = vg.findViewById(R.id.select_svsrssi);
        final Spinner spSvsLocation = vg.findViewById(R.id.svslocphoto_spinner_svsdevice);
        final TextView battery = vg.findViewById(R.id.textViewBattery);


        ImageButton imageButtonBattery = vg.findViewById(R.id.imageButtonBattery); // added by hslee   2020.04.28
        imageButtonBattery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, BetteryInfoGetterActivity.class);
                intent.putExtra("sensorEntity", device);
                intent.putExtra("sensorPosition", position);
                SVS.getInstance().bRegisterActivityNotRefresh = true;
                //intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                ((Activity) context).startActivityForResult(intent, DefConstant.REQUEST_SENSING_RESULT);
            }
        });

        battery.setText(String.valueOf(device.getBattery()));  // added by hslee   2020.05.02
        if( device.isbGetBatteryInfo() ) {  // 배터리 정보를 가져왔으면 이미지 갱신
            imageButtonBattery.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_battery_std_8da6d3_24dp));
        }

        cbSvs.setSelected(device.isChecked());
        cbSvs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cbSvs.setSelected(!cbSvs.isSelected());
                device.setChecked(cbSvs.isSelected());
            }
        });

        //Name
        tvName.setText("Name : " + device.getName());

        //Identifier (Address)
        tvAdd.setText("Identifier : " + device.getAddress());

        //RSSI
        if(device.isLinked())
        {
            tvRSSI.setText("Connected devices");
        }
        else if(device.getRssi() == Integer.MIN_VALUE)
        {
            tvRSSI.setText("No signal found");
        }
        else
        {
            tvRSSI.setText("Recent RSSI : " + device.getRssi() + "dBm");
        }


        int selection = (device.getSvsLocation() == null ? 0 : device.getSvsLocation().getIndex());

        spSvsLocation.setAdapter(svsLocAdapter);
        spSvsLocation.setSelection(selection);
        spSvsLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?>  parent, View view, int position, long id) {
                device.setSvsLocation(svsLocations.get(position));
            }
            public void onNothingSelected(AdapterView<?>  parent) {
            }
        });

        return vg;
    }
}
