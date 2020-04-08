package kr.co.signallink.svsv2.views.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmList;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.command.ParserCommand;
import kr.co.signallink.svsv2.commons.DefBLEdata;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.commons.DefFile;
import kr.co.signallink.svsv2.commons.DefLog;
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
import kr.co.signallink.svsv2.services.UartService;
import kr.co.signallink.svsv2.user.ConnectSVSItem;
import kr.co.signallink.svsv2.user.ConnectSVSItems;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.DateUtil;
import kr.co.signallink.svsv2.utils.DialogUtil;
import kr.co.signallink.svsv2.utils.FileUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;

// added by hslee 2020-03-23
public class MeasureModeSelectActivity extends Activity {

    private static final String TAG = "MeasureModeSelectActivity";

    String uuid;
    private SVS svs = SVS.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_measure_mode);

        uuid = getIntent().getStringExtra("equipmentUuid");

        Button buttonConfirm = findViewById(R.id.buttonConfirm);
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton radioButtonPipe = findViewById(R.id.radioButtonPipe);
                RadioButton radioButtonSensor = findViewById(R.id.radioButtonSensor);

                if( radioButtonPipe.isChecked() ) {
                    Intent intent = new Intent(getBaseContext(), PipePresetActivity.class);
                    intent.putExtra("equipmentUuid", uuid);
                    startActivity(intent);
                }
                else if( radioButtonSensor.isChecked() ) {
                    svs.setLinkedEquipmentUuid(uuid);
                    svs.setSelectedEquipmentUuid(uuid);

                    RealmList<SVSEntity> svsEntityRealmList = ((EquipmentEntity)svs.getSelectedEquipmentData()).getSvsEntities();
                    if( svsEntityRealmList == null || svsEntityRealmList.size() != 3 ) {
                        ToastUtil.showShort("3 sensor required.");
                        return;
                    }

                    Intent intent = new Intent(getBaseContext(), PresetActivity.class);
                    intent.putExtra("equipmentUuid", uuid);
                    startActivity(intent);
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
