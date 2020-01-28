package kr.co.signallink.svsv2.views.adapters.viewholders;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.databases.SVSEntity;

public class SVSListViewHolder extends RecyclerView.ViewHolder {

    public LinearLayout linearLayout;
    public ImageView ivSvsImage;
    public TextView tvSvsName;
    public TextView tvCheckState;
    public TextView tvLastRecord;
    public TextView tvPlcState;
    public TextView tvMeasureType;

    public SVSEntity data;

    public SVSListViewHolder(View view){
        super(view);

        linearLayout = view.findViewById(R.id.itemlayout_svslocphoto_list_element);
        ivSvsImage = view.findViewById(R.id.image_svslocphoto_list_element);
        tvSvsName = view.findViewById(R.id.svslocphoto_svslocphoto_list_element);
        tvCheckState = view.findViewById(R.id.checkstate_svslocphoto_list_element);
        tvLastRecord = view.findViewById(R.id.lastrecord_svslocphoto_list_element);
        tvPlcState = view.findViewById(R.id.plcstate_svslocphoto_list_element);
        tvMeasureType = view.findViewById(R.id.measureType_svslocphoto_list_element);
    }
}
