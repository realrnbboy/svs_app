package kr.co.signallink.svsv2.views.adapters.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.databases.EquipmentEntity;
import kr.co.signallink.svsv2.databases.web.WEquipmentEntity;

public class WEquipmentListViewHolder extends RecyclerView.ViewHolder {

    public LinearLayout linearLayout;
    public ImageView ivKeyImage;
    public TextView tvName;
    public TextView tvComanyName;
    public TextView tvFactoryName;
    public TextView tvCreated;

    public WEquipmentEntity data;

    public WEquipmentListViewHolder(View view){
        super(view);

        linearLayout = view.findViewById(R.id.itemlayout_equipment_list_element);
        ivKeyImage = view.findViewById(R.id.keyimage_equipment_list_element);
        tvName = view.findViewById(R.id.name_equipment_list_element);
        tvComanyName = view.findViewById(R.id.company_name_equipment_list_element);
        tvFactoryName = view.findViewById(R.id.factory_name_equipment_list_element);
        tvCreated = view.findViewById(R.id.created_equipment_list_element);
    }
}
