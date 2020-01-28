package kr.co.signallink.svsv2.views.adapters.viewholders;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.databases.PresetEntity;

public class PresetListViewHolder extends RecyclerView.ViewHolder {

    public LinearLayout layoutPresetItem;
    public TextView textViewName;
    public TextView textViewCode;
    public TextView textViewEquipmentName;
    public ImageView imageViewSelectPreset;

    public PresetEntity data;

    public PresetListViewHolder(View view){
        super(view);

        layoutPresetItem = view.findViewById(R.id.layoutPresetItem);
        textViewName = view.findViewById(R.id.textViewName);
        textViewCode = view.findViewById(R.id.textViewCode);
        textViewEquipmentName = view.findViewById(R.id.textViewEquipmentName);
        imageViewSelectPreset = view.findViewById(R.id.imageViewSelectPreset);
    }
}
