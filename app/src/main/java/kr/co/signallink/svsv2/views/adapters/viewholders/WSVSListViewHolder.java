package kr.co.signallink.svsv2.views.adapters.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.databases.SVSEntity;
import kr.co.signallink.svsv2.databases.web.WSVSEntity;

public class WSVSListViewHolder extends RecyclerView.ViewHolder {

    public LinearLayout linearLayout;
    public ImageView ivSvsImage;
    public TextView tvSvsName;
    public TextView tvModel;
    public TextView tvSerailNumber;
    public TextView tvPosition;

    public WSVSEntity data;

    public WSVSListViewHolder(View view){
        super(view);

        linearLayout = view.findViewById(R.id.itemlayout_svslocphoto_list_element);
        ivSvsImage = view.findViewById(R.id.image_svslocphoto_list_element);
        tvSvsName = view.findViewById(R.id.svslocphoto_svslocphoto_list_element);
        tvModel = view.findViewById(R.id.model_svslocphoto_list_element);
        tvSerailNumber = view.findViewById(R.id.serialnumber_svslocphoto_list_element);
        tvPosition = view.findViewById(R.id.position_svslocphoto_list_element);
    }
}
