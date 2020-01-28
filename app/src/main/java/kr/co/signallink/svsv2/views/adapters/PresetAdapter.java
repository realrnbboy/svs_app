package kr.co.signallink.svsv2.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import io.realm.OrderedRealmCollection;
import io.realm.RealmList;
import io.realm.RealmRecyclerViewAdapter;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.databases.PresetEntity;
import kr.co.signallink.svsv2.databases.SVSEntity;
import kr.co.signallink.svsv2.services.MyApplication;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.StringUtil;
import kr.co.signallink.svsv2.views.adapters.viewholders.PresetListViewHolder;

public class PresetAdapter extends RealmRecyclerViewAdapter<PresetEntity, PresetListViewHolder> {

    private SVS svs = SVS.getInstance();
    private RequestManager mGlideRequestManager;

    private int selectNo = -1;
    public HashMap selectedMap = new HashMap();

    public PresetAdapter(OrderedRealmCollection<PresetEntity> entities){
        super(entities, true);

        mGlideRequestManager = Glide.with(MyApplication.getInstance().getAppContext());
    }

    //선택된 아이템 no 저장
    public void setSelectedNo(int no){
        selectNo = no;
    }


    @Override
    public PresetListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.preset_list_item, viewGroup, false);
        return new PresetListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final PresetListViewHolder viewHolder, final int position) {

        //아이템
        final PresetEntity presetEntity = getItem(position);
        viewHolder.data = presetEntity;

        //이름
        viewHolder.textViewName.setText(presetEntity.getName()==null?"null":presetEntity.getName());

        //code
        //viewHolder.textViewCode.setText(presetEntity.getCode());

        //equipment name
        viewHolder.textViewEquipmentName.setText(presetEntity.getEquipmentName());

        //선택한 아이템 (마지막에 선택된 아이템은 배경을 변경. 검정색->주황색)
        if( selectNo == presetEntity.getNo() ) {
            viewHolder.layoutPresetItem.setBackgroundResource(R.drawable.element_pressed);
        } else {
            viewHolder.layoutPresetItem.setBackgroundResource(R.drawable.element_normal);
        }

        viewHolder.imageViewSelectPreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean bSelected = viewHolder.imageViewSelectPreset.isSelected();
                if( bSelected ) {  // 상태가 선택된 상태였으면
                    selectedMap.remove(position+"");
                }
                else {
                    selectedMap.put(position+"", presetEntity);
                }

                viewHolder.imageViewSelectPreset.setSelected(!bSelected);
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}


