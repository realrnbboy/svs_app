package kr.co.signallink.svsv2.views.adapters.viewholders;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.databases.EquipmentEntity;

public class EquipmentListViewHolder extends RecyclerView.ViewHolder {

    public LinearLayout linearLayout;
    public ImageView ivKeyImage;
    public TextView tvName;
    public TextView tvCheckState;
    public TextView tvLocation;
    public TextView tvSvsLocationNumber;
    public TextView tvLastRecord;

    public EquipmentEntity data;

    public EquipmentListViewHolder(View view){
        super(view);

        linearLayout = view.findViewById(R.id.itemlayout_equipment_list_element);
        ivKeyImage = view.findViewById(R.id.keyimage_equipment_list_element);
        tvName = view.findViewById(R.id.name_equipment_list_element);
        tvCheckState = view.findViewById(R.id.checkstate_equipment_list_element);
        tvLocation = view.findViewById(R.id.location_equipment_list_element);
        tvSvsLocationNumber = view.findViewById(R.id.svslocationnumber_equipment_list_element);
        tvLastRecord = view.findViewById(R.id.lastrecord_equipment_list_element);
    }
}
