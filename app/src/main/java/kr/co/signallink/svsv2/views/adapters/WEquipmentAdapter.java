package kr.co.signallink.svsv2.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import io.realm.OrderedRealmCollection;
import io.realm.RealmList;
import io.realm.RealmRecyclerViewAdapter;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.databases.EquipmentEntity;
import kr.co.signallink.svsv2.databases.SVSEntity;
import kr.co.signallink.svsv2.databases.web.WEquipmentEntity;
import kr.co.signallink.svsv2.services.MyApplication;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.StringUtil;
import kr.co.signallink.svsv2.views.adapters.viewholders.EquipmentListViewHolder;
import kr.co.signallink.svsv2.views.adapters.viewholders.WEquipmentListViewHolder;

import static kr.co.signallink.svsv2.commons.DefConstant.API_URL;
import static kr.co.signallink.svsv2.commons.DefConstant.WEB_URL;

public class WEquipmentAdapter extends RealmRecyclerViewAdapter<WEquipmentEntity, WEquipmentListViewHolder> {

    private SVS svs = SVS.getInstance();
    private RequestManager mGlideRequestManager;

    private String selectedEquipmentUuid = null;

    public WEquipmentAdapter(OrderedRealmCollection<WEquipmentEntity> entities){
        super(entities, true);

        mGlideRequestManager = Glide.with(MyApplication.getInstance().getAppContext());
    }

    //선택된 Equipment UUID 저장
    public void setSelectedEquipmentUuid(String uuid){
        selectedEquipmentUuid = uuid;
    }


    @Override
    public WEquipmentListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.wequipment_list_element, viewGroup, false);
        return new WEquipmentListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull WEquipmentListViewHolder viewHolder, int position) {

        //아이템
        final WEquipmentEntity wEquipmentEntity = getItem(position);
        viewHolder.data = wEquipmentEntity;

        //이미지
        String url = API_URL + "files/company/" + wEquipmentEntity.company_id + "/equipment/thumbnail/"+wEquipmentEntity.picture;
        mGlideRequestManager.load(url).fitCenter().into(viewHolder.ivKeyImage);

        //장비 이름
        viewHolder.tvName.setText(wEquipmentEntity.name);

        //장비 제조사
        viewHolder.tvComanyName.setText(wEquipmentEntity.company_name);

        //장비 공장
        viewHolder.tvFactoryName.setText(wEquipmentEntity.factory_name);

        //생성일
        viewHolder.tvCreated.setText(wEquipmentEntity.created);

        //선택한 장비 (마지막에 선택된 장비는 배경을 변경. 검정색->주황색)
        if(StringUtil.equalNotEmpty(selectedEquipmentUuid, wEquipmentEntity.id)) {
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


