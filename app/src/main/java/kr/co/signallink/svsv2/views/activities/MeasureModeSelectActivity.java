package kr.co.signallink.svsv2.views.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.command.ParserCommand;
import kr.co.signallink.svsv2.commons.DefBLEdata;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.commons.DefFile;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.databases.AnalysisEntity;
import kr.co.signallink.svsv2.databases.DatabaseUtil;
import kr.co.signallink.svsv2.databases.EquipmentEntity;
import kr.co.signallink.svsv2.databases.HistoryData;
import kr.co.signallink.svsv2.databases.SVSEntity;
import kr.co.signallink.svsv2.databases.dao.RealmDao;
import kr.co.signallink.svsv2.dto.MeasureData;
import kr.co.signallink.svsv2.dto.SVSCode;
import kr.co.signallink.svsv2.dto.SVSParam;
import kr.co.signallink.svsv2.dto.SVSTime;
import kr.co.signallink.svsv2.dto.UploadData;
import kr.co.signallink.svsv2.model.RmsModel;
import kr.co.signallink.svsv2.services.UartService;
import kr.co.signallink.svsv2.user.ConnectSVSItem;
import kr.co.signallink.svsv2.user.ConnectSVSItems;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.DateUtil;
import kr.co.signallink.svsv2.utils.DialogUtil;
import kr.co.signallink.svsv2.utils.FileUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.utils.Utils;

// added by hslee 2020-03-23
// 모드 선택
public class MeasureModeSelectActivity extends Activity {

    private static final String TAG = "MeasureModeSelectActivity";

    String uuid;
    String type;    // 측정 모드 선택, 내역 모드 선택
    private SVS svs = SVS.getInstance();

    @Override
    public void setRequestedOrientation(int requestedOrientation) {

        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            super.setRequestedOrientation(requestedOrientation);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_measure_mode);

        type = getIntent().getStringExtra("type");
        uuid = getIntent().getStringExtra("equipmentUuid");

        Button buttonConfirm = findViewById(R.id.buttonConfirm);
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton radioButtonPipe = findViewById(R.id.radioButtonPipe);
                RadioButton radioButtonSensor = findViewById(R.id.radioButtonSensor);

                if( "history".equals(type) ) {  // 분석 내역 보기 일때
                    if (radioButtonPipe.isChecked()) {
                        try {
                            String endd = Utils.getCurrentTime("yyyy-MM-dd");
                            String tEndd = Utils.addDateDay(endd, 1, "yyyy-MM-dd"); // 00시부터 계산하기 때문에 다음날 0시이전의 데이터를 가져오기 위해 +1해줌
                            String startd = Utils.addDateDay(endd, -7, "yyyy-MM-dd");

                            Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startd);
                            Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(tEndd);

                            long startLong = startDate.getTime();
                            long endLong = endDate.getTime();

                            Realm realm = Realm.getDefaultInstance();

                            RealmResults<AnalysisEntity> preiviousAnalysisEntityList = realm.where(AnalysisEntity.class)
                                    .equalTo("type", 1) // 1 rms, 2 frequency
                                    .greaterThanOrEqualTo("created", startLong)
                                    .lessThanOrEqualTo("created", endLong)
                                    .equalTo("equipmentUuid", uuid)
                                    .findAll()
                                    //.sort("created", Sort.DESCENDING);
                                    .sort("created", Sort.ASCENDING);

                            ArrayList<RmsModel> rmsModelList = new ArrayList<>();

                            for( AnalysisEntity analysisEntity : preiviousAnalysisEntityList ) {
                                RmsModel rmsModel = new RmsModel();
                                rmsModel.setRms1(analysisEntity.getRms1());
                                rmsModel.setRms2(analysisEntity.getRms2());
                                rmsModel.setRms3(analysisEntity.getRms3());
                                rmsModel.setbShowCause(analysisEntity.isbShowCause());
                                rmsModel.setCreated(analysisEntity.getCreated());

                                rmsModelList.add(rmsModel);
                            }


                            // 다음 화면으로 이동
                            Intent intent = new Intent(getBaseContext(), RecordManagerActivity.class);
                            intent.putExtra("equipmentUuid", uuid);
                            intent.putExtra("previousRmsModelList", rmsModelList);
                            startActivity(intent);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (radioButtonSensor.isChecked()) {
                        try {
                            String endd = Utils.getCurrentTime("yyyy-MM-dd");
                            String tEndd = Utils.addDateDay(endd, 1, "yyyy-MM-dd"); // 00시부터 계산하기 때문에 다음날 0시이전의 데이터를 가져오기 위해 +1해줌
                            String startd = Utils.addDateDay(endd, -7, "yyyy-MM-dd");

                            Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startd);
                            Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(tEndd);

                            long startLong = startDate.getTime();
                            long endLong = endDate.getTime();

                            Realm realm = Realm.getDefaultInstance();

                            RealmResults<AnalysisEntity> preiviousAnalysisEntityList = realm.where(AnalysisEntity.class)
                                    .equalTo("type", 2) // 1 rms, 2 frequency
                                    .greaterThanOrEqualTo("created", startLong)
                                    .lessThanOrEqualTo("created", endLong)
                                    .equalTo("equipmentUuid", uuid)
                                    .findAll()
                                    //.sort("created", Sort.DESCENDING);
                                    .sort("created", Sort.ASCENDING);

                            ArrayList<RmsModel> rmsModelList = new ArrayList<>();

                            for( AnalysisEntity analysisEntity : preiviousAnalysisEntityList ) {
                                RmsModel rmsModel = new RmsModel();
                                rmsModel.setRms1(analysisEntity.getRms1());
                                rmsModel.setCreated(analysisEntity.getCreated());

                                rmsModelList.add(rmsModel);
                            }

                            // 다음 화면으로 이동
                            Intent intent = new Intent(getBaseContext(), PipeRecordManagerActivity.class);
                            intent.putExtra("equipmentUuid", uuid);
                            intent.putExtra("previousRmsModelList", rmsModelList);
                            startActivity(intent);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {  // 측정모드 일 때
                    if (radioButtonPipe.isChecked()) {
                        Intent intent = new Intent(getBaseContext(), PipePresetActivity.class);
                        intent.putExtra("equipmentUuid", uuid);
                        startActivity(intent);
                    } else if (radioButtonSensor.isChecked()) {
                        svs.setLinkedEquipmentUuid(uuid);
                        svs.setSelectedEquipmentUuid(uuid);

                        RealmList<SVSEntity> svsEntityRealmList = ((EquipmentEntity) svs.getSelectedEquipmentData()).getSvsEntities();
                        if (svsEntityRealmList == null || svsEntityRealmList.size() != 3) {
                            ToastUtil.showShort("3 sensor required.");
                            return;
                        }

                        Intent intent = new Intent(getBaseContext(), PresetActivity.class);
                        intent.putExtra("equipmentUuid", uuid);
                        startActivity(intent);
                    }
                }

                finish();
            }
        });

        Button buttonCancel = findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
