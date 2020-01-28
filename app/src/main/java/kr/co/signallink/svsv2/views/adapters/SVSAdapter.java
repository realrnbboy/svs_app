package kr.co.signallink.svsv2.views.adapters;

import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.databases.SVSEntity;
import kr.co.signallink.svsv2.services.MyApplication;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.views.adapters.viewholders.SVSListViewHolder;

public class SVSAdapter extends RealmRecyclerViewAdapter<SVSEntity, SVSListViewHolder> {

    private SVS svs = SVS.getInstance();
    private RequestManager mGlideRequestManager;

    private String selectedSvsUuid = null;

    public SVSAdapter(OrderedRealmCollection<SVSEntity> entities){
        super(entities, true);

        mGlideRequestManager = Glide.with(MyApplication.getInstance().getAppContext());
    }

    //선택된 SVS UUID 저장
    public void setSelectedSvsUuid(String uuid){
        this.selectedSvsUuid = uuid;
    }

    @NonNull
    @Override
    public SVSListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.svslocphoto_list_element, viewGroup, false);
        return new SVSListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SVSListViewHolder viewHolder, int position) {

        //아이템
        final SVSEntity svsEntity = getItem(position);
        viewHolder.data = svsEntity;


        //이미지
        mGlideRequestManager.load(svsEntity.getImageUri()).fitCenter().into(viewHolder.ivSvsImage);

        //이름
        if(svsEntity.getSvsLocation() != null){
            viewHolder.tvSvsName.setText(svsEntity.getSvsLocation().toString().toUpperCase());
        }else {
            viewHolder.tvSvsName.setText("");
        }

        //상태
        DefConstant.SVS_STATE svsState = svsEntity.getSvsState();
        if(svsState != null){
            viewHolder.tvCheckState.setTextColor(svsState.getIntColor());
            viewHolder.tvCheckState.setText(svsState.toString());
        }else {
            viewHolder.tvCheckState.setText("");
        }

        //마지막 저장된 데이터 정보
        viewHolder.tvLastRecord.setText(svsEntity.getLastRecord());

        //PLCState
        DefConstant.PLCState plcState = svsEntity.getPlcState();
        if(plcState != null){
            viewHolder.tvPlcState.setText(plcState.name());
        }else {
            viewHolder.tvPlcState.setText("");
        }

        //Measure Option
        DefConstant.MEASURE_OPTION measureOption = svsEntity.getMeasureOption();
        if(measureOption != null){
            viewHolder.tvMeasureType.setText(measureOption.toString());
        }else {
            viewHolder.tvMeasureType.setText("");
        }

        //선택된 배경 변경
        if(selectedSvsUuid != null && svsEntity.getUuid() != null && svsEntity.getUuid().equals(selectedSvsUuid)) {
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


