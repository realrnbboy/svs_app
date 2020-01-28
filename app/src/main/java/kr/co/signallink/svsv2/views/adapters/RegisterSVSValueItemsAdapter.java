package kr.co.signallink.svsv2.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefFile;
import kr.co.signallink.svsv2.user.RegisterSVSItem;

public class RegisterSVSValueItemsAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<RegisterSVSItem> devices;
    private ArrayAdapter<DefFile.SVS_LOCATION> svsLocAdapter;
    private List<DefFile.SVS_LOCATION> svsLocations;

    public RegisterSVSValueItemsAdapter(Context context, List<RegisterSVSItem> devices) {
        this.context = context;
        inflater = LayoutInflater.from(context);

        this.devices = devices;
        svsLocations = DefFile.SVS_LOCATION.getArrayList();
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
    public View getView(int position, View convertView, ViewGroup parent) {

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
