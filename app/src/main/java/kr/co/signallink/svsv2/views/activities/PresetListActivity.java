package kr.co.signallink.svsv2.views.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.databases.DatabaseUtil;
import kr.co.signallink.svsv2.databases.PresetEntity;
import kr.co.signallink.svsv2.dto.AnalysisData;
import kr.co.signallink.svsv2.model.DIAGNOSIS_DATA_Type;
import kr.co.signallink.svsv2.model.MATRIX_2_Type;
import kr.co.signallink.svsv2.model.MainData;
import kr.co.signallink.svsv2.server.SendPost;
import kr.co.signallink.svsv2.services.DiagnosisInfo;
import kr.co.signallink.svsv2.services.SendMessageHandler;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.DialogUtil;
import kr.co.signallink.svsv2.utils.ItemClickUtil;
import kr.co.signallink.svsv2.utils.SharedUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.utils.Utils;
import kr.co.signallink.svsv2.views.adapters.PresetAdapter;
import kr.co.signallink.svsv2.views.custom.CustomListDialog;

// added by hslee 2020-01-15
// preset 목록 화면 - not used
public class PresetListActivity extends BaseActivity {

    private static final String TAG = "PresetListActivity";

    private Toolbar svsToolbar;
    
    //View
    private Button buttonAddPreset;
    private RecyclerView presetRecyclerView = null;
    private PresetAdapter presetAdapter;
    private CustomListDialog customListDialog;

    //Data
    private SVS svs = SVS.getInstance();
    private OrderedRealmCollection<PresetEntity> presetEntityList;

    MainData mainData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preset_list);

        customListDialog = new CustomListDialog(this, R.array.custom_list_preset);

        initViews();

        initAdapter();

        // 다른 곳에 있어야함. for test
        mainData = new MainData(this);
    }

    public void makeMatrix2() {

//        DiagnosisInfo diagnosis = new DiagnosisInfo(mainData);
//
//        DIAGNOSIS_DATA_Type[] rawData = mainData.fnGetRawDatas();
//
//        MATRIX_2_Type testResult = diagnosis.fnMakeMatrix2(rawData[0], rawData[1], rawData[2]);

        int i = 0;
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DefLog.d(TAG, "onDestroy()");
    }

    @Override
    public void onResume() {
        super.onResume();

        DefLog.d(TAG, "onResume");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void initViews() {
        
        setContentView(R.layout.activity_preset_list);

        svsToolbar = findViewById(R.id.toolbar);
        ((TextView)svsToolbar.findViewById(R.id.toolbar_title)).setText("Preset");
        setSupportActionBar(svsToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        findViewById(R.id.button_record).setVisibility(View.GONE);  // 안쓰는 버튼 숨김

        //등록 버튼
//        buttonAddPreset = findViewById(R.id.buttonAddPreset);
//        buttonAddPreset.setVisibility(View.VISIBLE);
//        buttonAddPreset.setOnClickListener(new Button.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                goIntent(PresetActivity.class);
//            }
//        });

//        ImageView imageViewRemovePreset = findViewById(R.id.imageViewRemovePreset);
//        imageViewRemovePreset.setVisibility(View.VISIBLE);
//        imageViewRemovePreset.setOnClickListener(new Button.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                removeItems();
//            }
//        });
    }

    private void initAdapter(){

        //리스트 어댑터
        Realm realm = Realm.getDefaultInstance();
        presetEntityList = realm.where(PresetEntity.class).findAll().sort("no", Sort.DESCENDING);
        presetAdapter = new PresetAdapter(presetEntityList);

        //리스트 뷰
        presetRecyclerView = findViewById(R.id.recyclerViewPreset);
        presetRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        presetRecyclerView.setAdapter(presetAdapter);
        ItemClickUtil.addTo(presetRecyclerView)
                .setOnItemClickListener(onItemClickListener)
                .setOnItemLongClickListener(onItemLongClickListener);

    }


    private ItemClickUtil.OnItemLongClickListener onItemLongClickListener = new ItemClickUtil.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {

            PresetEntity presetEntity = presetEntityList.get(position);
            int no = presetEntity.getNo();

            presetAdapter.setSelectedNo(no);
            presetAdapter.notifyDataSetChanged();

            // 아이템 선택 및 다음 버튼 클릭으로 간주하고 다음 화면을 보여줌

            return true;
        }
    };

    // 목록 중, 선택된 아이템들을 삭제한다.
    private void removeItems() {
        int selectedCount = presetAdapter.selectedMap.size();
        if( selectedCount <= 0 ) {  // 선택된 것이 없는 경우
            ToastUtil.showShort("please select item for remove");
            return;
        }

        if( selectedCount > presetEntityList.size() ) { // 오류 상황, 선택된 것이 목록 개수보다 많은 경우
            ToastUtil.showShort("invalid list");
            return;
        }

        DialogUtil.yesNo(this,
                "Preset remove",
                "Do you want to remove?",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        remove();
                    }
                },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
    }

    // db에서 삭제 수행
    private void remove() {

        DatabaseUtil.transaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                int removeCount = 0;
                Iterator<String> keys = presetAdapter.selectedMap.keySet().iterator();
                while( keys.hasNext() ){
                    String key = keys.next();
                    PresetEntity presetEntity = (PresetEntity)presetAdapter.selectedMap.get(key);
                    if( presetEntity == null )
                        continue;

                    // 조건에 맞는 데이터 가져오기
                    RealmResults<PresetEntity> result = realm.where(PresetEntity.class).equalTo("no", presetEntity.getNo()).findAll();

                    removeCount = result.size();

                    // db에서 삭제
                    result.deleteAllFromRealm();
                }

                if( removeCount > 0 ) {
                    ToastUtil.showShort("Remove success");
                }
                else {
                    ToastUtil.showShort("Remove failed");
                }
            }
        });
    }


    private ItemClickUtil.OnItemClickListener onItemClickListener = new ItemClickUtil.OnItemClickListener() {
        @Override
        public void onItemClicked(RecyclerView recyclerView, final int position, View v) {

            final PresetEntity presetEntity = presetEntityList.get(position);
            int no = presetEntity.getNo();

            presetAdapter.setSelectedNo(no);
            presetAdapter.notifyDataSetChanged();


            customListDialog.setDialogListener(new CustomListDialog.OnCustomListButtonDialogListener() {
                @Override
                public void onBtnClicked(int viewIdx) {

                    if( viewIdx == 1 ) {    // next 클릭
//                        Intent intent = new Intent(getBaseContext(), AnalysisResultActivity.class);
//                        AnalysisData analysisData = setAnalysisData(presetEntity);
//                        intent.putExtra("analysisData", analysisData);
//                        startActivity(intent);
                    }
                    else if( viewIdx == 2 ) {    // update 클릭

                        Intent intent = new Intent(getBaseContext(), PresetActivity.class);
                        AnalysisData analysisData = setAnalysisData(presetEntity);
                        intent.putExtra("bModeCreate", "0");
                        intent.putExtra("analysisData", analysisData);
                        startActivity(intent);
                    }
                }

                @Override
                public void onBtnCancelClicked() {
                }
            });

            customListDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {

                    //presetAdapter.setSelectedNo(svs.getSelectedEquipmentUuid());
                    //presetAdapter.notifyDataSetChanged();
                }
            });

            customListDialog.show();
        }
    };

    AnalysisData setAnalysisData(PresetEntity presetEntity) {
        AnalysisData analysisData = new AnalysisData();

        analysisData.setNo(presetEntity.getNo());
        analysisData.setName(presetEntity.getName());
        analysisData.setCode(presetEntity.getCode());
        analysisData.setEquipmentType(presetEntity.getBearingType());
        analysisData.setBearingType(presetEntity.getBearingType());
        analysisData.setLineFreq(presetEntity.getLineFreq());
        analysisData.setProjectVibSpec(presetEntity.getProjectVibSpec());
        analysisData.setSiteCode(presetEntity.getSiteCode());
        analysisData.setEquipmentName(presetEntity.getEquipmentName());
        analysisData.setTagNo(presetEntity.getTagNo());
        analysisData.setInputPower(presetEntity.getInputPower());
        analysisData.setRpm(presetEntity.getRpm());
        analysisData.setBladeCount(presetEntity.getBladeCount());
        analysisData.setBallCount(presetEntity.getBallCount());
        analysisData.setPitchDiameter(presetEntity.getPitchDiameter());
        analysisData.setBallDiameter(presetEntity.getBallDiameter());
        analysisData.setRps(presetEntity.getRps());
        analysisData.setContactAngle(presetEntity.getContactAngle());

        return analysisData;
    }

}
