package kr.co.signallink.svsv2.views.adapters;

import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import io.realm.OrderedRealmCollection;
import io.realm.RealmList;
import io.realm.RealmRecyclerViewAdapter;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.commons.DefFile;
import kr.co.signallink.svsv2.databases.EquipmentEntity;
import kr.co.signallink.svsv2.databases.SVSEntity;
import kr.co.signallink.svsv2.services.MyApplication;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.StringUtil;
import kr.co.signallink.svsv2.views.adapters.viewholders.EquipmentListViewHolder;

public class EquipmentAdapter extends RealmRecyclerViewAdapter<EquipmentEntity, EquipmentListViewHolder> {

    private SVS svs = SVS.getInstance();
    private RequestManager mGlideRequestManager;

    private String selectedEquipmentUuid = null;

    public EquipmentAdapter(OrderedRealmCollection<EquipmentEntity> entities){
        super(entities, true);

        mGlideRequestManager = Glide.with(MyApplication.getInstance().getAppContext());
    }

    //선택된 Equipment UUID 저장
    public void setSelectedEquipmentUuid(String uuid){
        selectedEquipmentUuid = uuid;
    }


    @Override
    public EquipmentListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.equipment_list_element, viewGroup, false);
        return new EquipmentListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EquipmentListViewHolder viewHolder, int position) {

        //아이템
        final EquipmentEntity equipmentEntity = getItem(position);
        viewHolder.data = equipmentEntity;

        //이미지
        mGlideRequestManager.load(equipmentEntity.getImageUri()).fitCenter().into(viewHolder.ivKeyImage);

        //장비 이름
        viewHolder.tvName.setText(equipmentEntity.getName());

        //장비 상태
        DefConstant.SVS_STATE svsState = equipmentEntity.getSvsState();
        if(svsState != null){
            viewHolder.tvCheckState.setTextColor(svsState.getIntColor());
            viewHolder.tvCheckState.setText(svsState.toString());
        }else {
            viewHolder.tvCheckState.setText("");
        }


        //장비 위치
        viewHolder.tvLocation.setText(equipmentEntity.getLocation());

        //SVS 정보 (연결된 기기 이름, 총 기기 갯수)
        RealmList<SVSEntity> svsEntities = equipmentEntity.getSvsEntities();
        if(svsEntities != null){
            String str = String.format("(#%d)", svsEntities.size());
            for(SVSEntity svsEntity : svsEntities)
            {
                if(StringUtil.equalNotEmpty(svsEntity.getUuid(), svs.getLinkedSvsUuid()))
                {
                    DefFile.SVS_LOCATION svsLocation = svsEntity.getSvsLocation();
                    str = String.format("%s (#%d)", svsLocation.toString().toUpperCase(), svsEntities.size());
                    break;
                }
            }
            viewHolder.tvSvsLocationNumber.setText(str);
        }else {
            viewHolder.tvSvsLocationNumber.setText("");
        }


        //저장된 데이터
        viewHolder.tvLastRecord.setText(equipmentEntity.getLastRecord());

        //선택한 장비 (마지막에 선택된 장비는 배경을 변경. 검정색->주황색)
        if(StringUtil.equalNotEmpty(selectedEquipmentUuid, equipmentEntity.getUuid()))
        {
            viewHolder.linearLayout.setBackgroundResource(R.drawable.element_pressed);
        } else {
            viewHolder.linearLayout.setBackgroundResource(R.drawable.element_normal);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}


