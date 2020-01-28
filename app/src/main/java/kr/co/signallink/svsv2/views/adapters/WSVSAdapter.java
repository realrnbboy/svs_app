package kr.co.signallink.svsv2.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.databases.SVSEntity;
import kr.co.signallink.svsv2.databases.web.WSVSEntity;
import kr.co.signallink.svsv2.services.MyApplication;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.StringUtil;
import kr.co.signallink.svsv2.views.adapters.viewholders.SVSListViewHolder;
import kr.co.signallink.svsv2.views.adapters.viewholders.WSVSListViewHolder;

import static kr.co.signallink.svsv2.commons.DefConstant.API_URL;

public class WSVSAdapter extends RealmRecyclerViewAdapter<WSVSEntity, WSVSListViewHolder> {

    private SVS svs = SVS.getInstance();
    private RequestManager mGlideRequestManager;

    private String selectedSvsUuid = null;

    public WSVSAdapter(OrderedRealmCollection<WSVSEntity> entities){
        super(entities, true);

        mGlideRequestManager = Glide.with(MyApplication.getInstance().getAppContext());
    }

    //선택된 SVS UUID 저장
    public void setSelectedSvsUuid(String uuid){
        this.selectedSvsUuid = uuid;
    }

    @NonNull
    @Override
    public WSVSListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.wsvslocphoto_list_element, viewGroup, false);
        return new WSVSListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull WSVSListViewHolder viewHolder, int position) {

        //아이템
        final WSVSEntity wsvsEntity = getItem(position);
        viewHolder.data = wsvsEntity;


        //이미지
        String url = API_URL + "files/company/" + wsvsEntity.company_id + "/svs/thumbnail/"+wsvsEntity.picture;
        mGlideRequestManager.load(url).fitCenter().into(viewHolder.ivSvsImage);

        //이름
        viewHolder.tvSvsName.setText(wsvsEntity.name);

        //모델
        if(!StringUtil.isEmpty(wsvsEntity.model)){
            viewHolder.tvModel.setText(wsvsEntity.model);
        }

        //시리얼 번호
        if(!StringUtil.isEmpty(wsvsEntity.serial_no)) {
            viewHolder.tvSerailNumber.setText(wsvsEntity.serial_no);
        }

        //센서 위치
        if(!StringUtil.isEmpty(wsvsEntity.position)) {
            viewHolder.tvPosition.setText(wsvsEntity.position);
        }



        //선택된 배경 변경
        if(selectedSvsUuid != null && wsvsEntity.id != null && wsvsEntity.id.equals(selectedSvsUuid)) {
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


