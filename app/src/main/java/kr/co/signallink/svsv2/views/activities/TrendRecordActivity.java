package kr.co.signallink.svsv2.views.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefBLEdata;
import kr.co.signallink.svsv2.commons.DefCMDOffset;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.databases.DatabaseUtil;
import kr.co.signallink.svsv2.databases.EquipmentEntity;
import kr.co.signallink.svsv2.databases.SVSEntity;
import kr.co.signallink.svsv2.dto.RawMeasureData;
import kr.co.signallink.svsv2.utils.DateUtil;
import kr.co.signallink.svsv2.utils.DialogUtil;
import kr.co.signallink.svsv2.utils.FileUtil;
import kr.co.signallink.svsv2.commons.DefFile;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.commons.DefPhoto;
import kr.co.signallink.svsv2.databases.HistoryData;
import kr.co.signallink.svsv2.dto.MeasureData;
import kr.co.signallink.svsv2.dto.SVSCode;
import kr.co.signallink.svsv2.dto.SVSParam;
import kr.co.signallink.svsv2.dto.UploadData;
import kr.co.signallink.svsv2.utils.MyValueFormatter;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.IntentUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.views.custom.CustomListDialog;

/**
 * Created by nspil on 2018-02-13.
 */


public class TrendRecordActivity extends BaseActivity{

    public static final String TAG = "TrendRecordActivity";

    private SVS svs = SVS.getInstance();
    private CombinedChart combinedChart = null;

    private DefConstant.TrendValue trendValue;
    private Button button_trend_stop;
    private Button button_record;

    private Toolbar svsToolbar;
    private LinearLayout ll_trendrecord;
    private TextView alarm_trendrecord;
    private ImageButton btnLeftArrow, btnRightArrow;
    private Spinner datatype_spinner_trendrecord;
    private ImageView commentimage_trendrecord;
    private EditText comment_trendrecord;
    private Button btn_save_trendrecord;
    private Button btn_cancel_trendrecord;

    ArrayAdapter<DefConstant.TrendValue> arrayAdapter;

    private CustomListDialog customListDialog;

    private Uri fileUri;
    private String photoPath;

    private ArrayList<DefConstant.TrendValue> checkedMeasureDatas = new ArrayList<>();


    private ChartRecordActivity chartrecordActivity = (ChartRecordActivity) ChartRecordActivity.chartrecordActivity;

    private final BroadcastReceiver StatusChangeReceiverOnTrend= new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //*********************//
            if (action.equals(DefBLEdata.MEASURE_ARRIVE)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d(TAG, "MEASURE_ARRIVE");
                        update();
                    }
                });
            } else if (action.equals(DefBLEdata.DISCONNECTION_ARRIVE)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        DefLog.d(TAG, "DISCONNECTION_ARRIVE");
                        finish();
                    }
                });
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trendrecord_activity);

        svsToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(svsToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        trendValue = (DefConstant.TrendValue) getIntent().getSerializableExtra(TrendRecordActivity.EXTRA_STR_TREND_VALUE);
        ((TextView)svsToolbar.findViewById(R.id.toolbar_title)).setText(trendValue.toString());

        LocalBroadcastManager.getInstance(this).registerReceiver(StatusChangeReceiverOnTrend, makeUpdateIntentFilter());

        init_dialog();
        init_checkedMeasureDatas();
        init();

        ll_trendrecord = findViewById(R.id.ll_trendrecord);
        button_trend_stop = findViewById(R.id.button_control_ble);
        alarm_trendrecord = findViewById(R.id.alarm_trendrecord);
        datatype_spinner_trendrecord = findViewById(R.id.datatype_spinner_trendrecord);
        commentimage_trendrecord = findViewById(R.id.commentimage_trendrecord);
        comment_trendrecord = findViewById(R.id.comment_trendrecord);
        btn_save_trendrecord = findViewById(R.id.btn_save_trendrecord);
        btn_cancel_trendrecord = findViewById(R.id.btn_cancel_trendrecord);

        ll_trendrecord.setOnClickListener(myClickListener);
        btn_save_trendrecord.setOnClickListener(myClickListener);
        btn_cancel_trendrecord.setOnClickListener(myClickListener);
        commentimage_trendrecord.setOnClickListener(myPhotoClickListener);
        comment_trendrecord.addTextChangedListener(myTextChangeListener);

        arrayAdapter = new ArrayAdapter<DefConstant.TrendValue>(this, R.layout.spinner_item, checkedMeasureDatas);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        datatype_spinner_trendrecord.setAdapter(arrayAdapter);
        datatype_spinner_trendrecord.setSelection(arrayAdapter.getPosition(trendValue));

        datatype_spinner_trendrecord.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?>  parent, View view, int position, long id) {
                trendValue = checkedMeasureDatas.get(position);
                ((TextView)svsToolbar.findViewById(R.id.toolbar_title)).setText(trendValue.toString());
                drawChart();
            }
            public void onNothingSelected(AdapterView<?>  parent) {
            }
        });

        btnLeftArrow = findViewById(R.id.btnLeftArrow);
        btnLeftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int index = datatype_spinner_trendrecord.getSelectedItemPosition() - 1;
                if(index < 0)
                    index = checkedMeasureDatas.size()-1; //loop

                datatype_spinner_trendrecord.setSelection(index,true);
                arrayAdapter.notifyDataSetChanged();
            }
        });
        btnRightArrow = findViewById(R.id.btnRightArrow);
        btnRightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int index = datatype_spinner_trendrecord.getSelectedItemPosition() + 1;
                if(index >= checkedMeasureDatas.size())
                    index = 0; //loop

                datatype_spinner_trendrecord.setSelection(index, true);
                arrayAdapter.notifyDataSetChanged();
            }
        });

        drawChart();
        drawAlarm();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(StatusChangeReceiverOnTrend);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }

        combinedChart.setUnbindEnabled(true);
        combinedChart = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        DefLog.d(TAG, "onResume");
        button_record = findViewById(R.id.button_record);
        button_record.setVisibility(View.GONE);

        //button_trend_stop.setVisibility(View.GONE);

        comment_trendrecord.setText(svs.getRecordcomment());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    private void init_dialog() {
        customListDialog = new CustomListDialog(this, R.array.custom_list_dialog_image);
    }

    private View.OnClickListener myClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            hideKeyboard();
            switch (v.getId())
            {
                case R.id.ll_trendrecord :
                    break;
                case R.id.btn_cancel_trendrecord :
                    dialog_Cancel();
                    break;
                case R.id.btn_save_trendrecord :
                    if(svs.getRecordCount() < DefConstant.RECORDCOUNT_MAX) {
                        ToastUtil.showShort("Recording is not completed");
                    } else {
                        svs.setRecordcomment(comment_trendrecord.getText().toString());

                        dialog_Save();
                    }
                    break;
            }
        }
    };

    private View.OnClickListener myPhotoClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            customListDialog.setDialogListener(new CustomListDialog.OnCustomListButtonDialogListener() {
                @Override
                public void onBtnClicked(int viewIdx) {

                    if(viewIdx == 1)
                    {
                        int requestCode = DefPhoto.PICK.CAMERA_0.getGlobalIndex();
                        fileUri = IntentUtil.captureCamera(TrendRecordActivity.this, requestCode);
                    }
                    else if(viewIdx == 2)
                    {
                        int requestCode = DefPhoto.PICK.ALBUM_0.getGlobalIndex();
                        IntentUtil.selectAlbum(TrendRecordActivity.this, requestCode);
                    }
                    else if(viewIdx == 3)
                    {
                        displayDeletePhoto(0);
                    }

                }

                @Override
                public void onBtnCancelClicked() {

                }
            });
            customListDialog.show();
        }
    };

    private TextWatcher myTextChangeListener = new TextWatcher()
    {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable arg0) {
            svs.setRecordcomment(arg0.toString());
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != Activity.RESULT_OK)
            return;

        if(requestCode == DefPhoto.PICK.CAMERA_0.getGlobalIndex()) {
            displayPhoto();
        } else if (requestCode == DefPhoto.PICK.ALBUM_0.getGlobalIndex()) {
            fileUri = data.getData();
            displayPhoto();
        }
    }

    private void displayPhoto() {
        photoPath = FileUtil.getPath(this, fileUri);
        Glide.with(this).load(fileUri).fitCenter().into(commentimage_trendrecord);
    }

    private void displayDeletePhoto(int index) {
        photoPath = null;
        Glide.with(this).load(R.drawable.btn_imgadd).fitCenter().into(commentimage_trendrecord);
    }

    private void confirmCancel() {
        svs.setRecordcomment(null);
        svs.setRecorded(false);
        svs.clearRawMeasureDatas();
        svs.clearRecordMeasurDatas();
        chartrecordActivity.finish();

        goIntent(MonitoringTrendActivity.class, true);
    }

    private static IntentFilter makeUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(DefBLEdata.MEASURE_ARRIVE);
        intentFilter.addAction(DefBLEdata.DISCONNECTION_ARRIVE);

        return intentFilter;
    }

    private void init(){
        combinedChart = (CombinedChart)findViewById(R.id.CombnedChart_trendrecord);
        combinedChart.getDescription().setEnabled(false);
        combinedChart.setBackgroundColor(getResources().getColor(R.color.colorContent));
        combinedChart.setOnChartGestureListener(OCGL);
        combinedChart.setMaxVisibleValueCount(20);
        combinedChart.setNoDataText(getResources().getString(R.string.recordingchartdata));

        Legend l = combinedChart.getLegend();
        l.setTextColor(Color.WHITE);
        l.setWordWrapEnabled(false);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        YAxis rightAxis = combinedChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0);
        rightAxis.setTextColor(Color.WHITE);

        YAxis leftAxis = combinedChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0);
        leftAxis.setTextColor(Color.WHITE);

        XAxis xAxis = combinedChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                int index = (int)value;

                String str = "";
                try {
                    Date date = svs.getMeasureDatas().get(index).getCaptureTime();
                    str = DateUtil.convertDefaultDetailDate(date);
                } catch (Exception e) {
                    DefLog.d(TAG, e.toString());
                }
                return str;
            }
        });

        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(svs.getMeasureDatas().size()-1);
        xAxis.setGranularity(1.0f);
        xAxis.setTextColor(Color.WHITE);
    }

    private void init_checkedMeasureDatas() {
        checkedMeasureDatas.clear();

        SVSParam svsParam = svs.getUploaddata().getSvsParam();
        SVSCode svsCode = svsParam.getCode();

        if(svsCode.getlTempEna() != 0){
            checkedMeasureDatas.add(DefConstant.TrendValue.TEMPERATURE);
        }

        if(svsCode.getTimeEna().getdPeak() != 0) {
            checkedMeasureDatas.add(DefConstant.TrendValue.DPEAK);
        }

        if(svsCode.getTimeEna().getdRms() != 0) {
            checkedMeasureDatas.add(DefConstant.TrendValue.DRMS);
        }

        if(svsCode.getTimeEna().getdCrf() != 0) {
            checkedMeasureDatas.add(DefConstant.TrendValue.DCRF);
        }

        for(int i = 0; i< DefCMDOffset.BAND_MAX; i++) {
            if(svsCode.getFreqEna()[i].getdPeak() != 0) {
                checkedMeasureDatas.add(DefConstant.TrendType.PEAKS.getTrendValue(i));
            }
        }

        for(int i = 0; i< DefCMDOffset.BAND_MAX; i++) {
            if(svsCode.getFreqEna()[i].getdBnd() != 0) {
                checkedMeasureDatas.add(DefConstant.TrendType.BANDS.getTrendValue(i));
            }
        }
    }

    public void update() {

        runOnUiThread(new Runnable() {
            public void run() {
                drawChart();
                drawAlarm();
            }
        });
    }

    private void drawAlarm() {
        int count = svs.getRecordCount();
        if(count >= DefConstant.RECORDCOUNT_MAX)
            count = DefConstant.RECORDCOUNT_MAX;

        String str = String.valueOf(count) + "/" + String.valueOf(DefConstant.RECORDCOUNT_MAX);
        alarm_trendrecord.setText(str);
    }

    private void drawChart(){

        if(svs.getRecordMeasureDatas().size() < 2)
            return;

        LineData lineData = new LineData();

        XAxis xAxis = combinedChart.getXAxis();
        xAxis.setAxisMaximum(svs.getRecordMeasureDatas().size()-1);


        ArrayList<Float> valueList = new ArrayList<>();
        SVSParam svsParam = svs.getUploaddata().getSvsParam();
        SVSCode svsCode = svs.getUploaddata().getSvsParam().getCode();
        ArrayList<MeasureData> measuredatas = svs.getRecordMeasureDatas();

        if(trendValue == DefConstant.TrendValue.TEMPERATURE){
            //temperature trend
            try {
                for(int i = 0; i<svs.getRecordMeasureDatas().size(); i++){
                    valueList.add((float)svs.getRecordMeasureDatas().get(i).getlTempCurrent());
                }

                lineData.addDataSet(generateLineData(valueList));
                lineData.addDataSet(generateLineData(true, (float)svsParam.getlTpWrn()));
                lineData.addDataSet(generateLineData(false, (float)svsParam.getlTpDan()));

            } catch (Exception ex) {
                return;
            }
        }else if(trendValue == DefConstant.TrendValue.DPEAK){
            try {
                for(int i=0; i<measuredatas.size(); i++){
                    valueList.add(measuredatas.get(i).getSvsTime().getdPeak());
                }

                lineData.addDataSet(generateLineData(valueList));
                lineData.addDataSet(generateLineData(true, svsCode.getTimeWrn().getdPeak()));
                lineData.addDataSet(generateLineData(false, svsCode.getTimeDan().getdPeak()));
            } catch (Exception ex) {
                return;
            }
        }else if(trendValue == DefConstant.TrendValue.DRMS){
            try {
                for(int i=0; i<measuredatas.size(); i++){
                    valueList.add(measuredatas.get(i).getSvsTime().getdRms());
                }
                lineData.addDataSet(generateLineData(valueList));
                lineData.addDataSet(generateLineData(true, svsCode.getTimeWrn().getdRms()));
                lineData.addDataSet(generateLineData(false, svsCode.getTimeDan().getdRms()));
            } catch (Exception ex) {
                return;
            }
        }else if(trendValue == DefConstant.TrendValue.DCRF){
            try {
                for(int i=0; i<measuredatas.size(); i++){
                    valueList.add(measuredatas.get(i).getSvsTime().getdCrf());
                }
                lineData.addDataSet(generateLineData(valueList));
                lineData.addDataSet(generateLineData(true, svsCode.getTimeWrn().getdCrf()));
                lineData.addDataSet(generateLineData(false, svsCode.getTimeDan().getdCrf()));
            } catch (Exception ex) {
                return;
            }
        }else if(DefConstant.TrendType.PEAKS.hasTrendValue(trendValue)){
            try {
                int index = trendValue.getIndex();
                for(int i=0; i<measuredatas.size(); i++){
                    valueList.add(measuredatas.get(i).getSvsFreq()[index].getdPeak());
                }
                lineData.addDataSet(generateLineData(valueList));
                lineData.addDataSet(generateLineData(true, svsCode.getFreqWrn()[index].getdPeak()));
                lineData.addDataSet(generateLineData(false, svsCode.getFreqDan()[index].getdPeak()));
            } catch (Exception ex) {
                return;
            }
        }else if(DefConstant.TrendType.BANDS.hasTrendValue(trendValue)){
            try {
                int index = trendValue.getIndex();
                for(int i=0; i<measuredatas.size(); i++){
                    valueList.add(measuredatas.get(i).getSvsFreq()[index].getdBnd());
                }
                lineData.addDataSet(generateLineData(valueList));
                lineData.addDataSet(generateLineData(true, svsCode.getFreqWrn()[index].getdBnd()));
                lineData.addDataSet(generateLineData(false, svsCode.getFreqDan()[index].getdBnd()));
            } catch (Exception ex) {
                return;
            }
        }

        valueList.clear();

        CombinedData combinedData = new CombinedData();
        //lineData.setDrawValues(true);
        combinedData.setData(lineData);

        combinedChart.setData(combinedData);
        combinedChart.invalidate();
    }

    private LineDataSet generateLineData(boolean isWrn, float value){
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, value));
        entries.add(new Entry(svs.getRecordMeasureDatas().size()-1, value));

        LineDataSet lineDataSet = new LineDataSet(entries,isWrn == true? "Warning" : "Danger");
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setValueTextColor(Color.WHITE);
        lineDataSet.setDrawValues(false);
        lineDataSet.setHighlightEnabled(false);
        lineDataSet.setColor(isWrn == true ? Color.YELLOW : Color.RED);

        return lineDataSet;
    }

    private LineDataSet generateLineData(ArrayList<Float> valueList){
        ArrayList<Entry> entries = new ArrayList<>();

        for(int i=0; i<valueList.size(); i++){
            entries.add(new Entry(i, valueList.get(i)));
        }

        LineDataSet lineDataSet = new LineDataSet(entries, trendValue.toString());
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setColor(Color.GREEN);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setValueTextColor(Color.WHITE);
        lineDataSet.setDrawValues(true);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setValueFormatter(new MyValueFormatter());
        lineDataSet.setFillDrawable(getResources().getDrawable(R.drawable.trend_gradient));

        return lineDataSet;
    }

    private OnChartGestureListener OCGL = new OnChartGestureListener() {
        @Override
        public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

        }

        @Override
        public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

        }

        @Override
        public void onChartLongPressed(MotionEvent me) {
            combinedChart.fitScreen();
        }

        @Override
        public void onChartDoubleTapped(MotionEvent me) {

        }

        @Override
        public void onChartSingleTapped(MotionEvent me) {

        }

        @Override
        public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

        }

        @Override
        public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

        }

        @Override
        public void onChartTranslate(MotionEvent me, float dX, float dY) {

        }
    };


    private void save() {

        if(svs.getScreenMode() != DefConstant.SCREEN_MODE.LOCAL)
        {
            ToastUtil.showShort("Wrong Processing.");
            return;
        }

        EquipmentEntity equipmentEntity = (EquipmentEntity)svs.getLinkedEquipmentData();
        SVSEntity svsEntity = (SVSEntity)svs.getLinkedSvsData();
        String linkedEquipmentUuid = equipmentEntity.getUuid();
        String linkedSvsUuid = svsEntity.getUuid();

        if(linkedEquipmentUuid != null && linkedSvsUuid != null)
        {
            ArrayList<MeasureData> measureDatas = svs.getRecordMeasureDatas();
            ArrayList<RawMeasureData> rawMeasureDatas = new ArrayList<>();
            for(MeasureData measureData : measureDatas){
                rawMeasureDatas.add(new RawMeasureData(measureData.getCaptureTime(), measureData.getRawData()));
            }

            if(rawMeasureDatas.size() > 0)
            {
                Date date = rawMeasureDatas.get(0).getCaptureDate();
                String strDate = DateUtil.getDateStringBySimpleFormat(date);
                String strTime = DateUtil.getTimeStringBySimpleFormat(date);

                //Dir
                String historyDir = DefFile.FOLDER.HISTORY.getFullPath();
                String equipmentDir = historyDir + linkedEquipmentUuid + File.separator;
                String svsDir = equipmentDir + linkedSvsUuid + File.separator;
                String dateDir = svsDir + strDate + File.separator;
                String timeDir = dateDir + strTime + File.separator;

                //폴더 확인
                File fTimeDir = new File(timeDir);
                if (!fTimeDir.exists()) {
                    boolean b = fTimeDir.mkdirs();
                    if(!b){
                        ToastUtil.showShort("Can't Make File.");
                        return;
                    }
                }


                //Write Upload, Measure, Photo, Comment
                FileUtil.writeUpload(timeDir);
                FileUtil.writeRawMeasure(timeDir, rawMeasureDatas);
                FileUtil.copyFile(photoPath, timeDir + DefFile.NAME.RECORD + DefFile.EXT.JPG);
                FileUtil.writeComment(timeDir, svs.getRecordcomment());

                //상태
                //UploadData uploaddata = ParserCommand.rawupload(svs.getRawuploaddata());
                UploadData uploadData = svs.getUploaddata();

                HistoryData historyData = new HistoryData();
                historyData.setUploaddata(uploadData);
                DefConstant.SVS_STATE svsState = DefConstant.SVS_STATE.DEFAULT;
                if (svs.getRecordMeasureDatas() != null)
                {
                    historyData.calcAverageMeasure(svs.getRecordMeasureDatas());

                    svsState = FileUtil.calcSVSState(uploadData, historyData.getAveragemeasure());
                }

                //기록 갯수
                int totalHistoryCount = 0; //장비의 총 갯수
                int targetHistoryCount = 0; //연결된 기기의 갯수


                //기기의 기록 갯수 구하기
                for(SVSEntity child : equipmentEntity.getSvsEntities())
                {
                    String childUuid = child.getUuid();

                    int historyCount = FileUtil.getSubDirCount(equipmentDir + childUuid + File.separator);
                    if(childUuid.equals(linkedSvsUuid))
                    {
                        targetHistoryCount = historyCount;
                    }

                    totalHistoryCount += historyCount;
                }

                //갯수를 파일로 쓰기
                final String equipmentLastRecordContent = strDate + "(" + totalHistoryCount + ")";
                final String svsLastRecordContent = strDate + "(" + targetHistoryCount + ")";
                final DefConstant.SVS_STATE lastSvsState = svsState;

                DatabaseUtil.transaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {

                        EquipmentEntity equipmentEntity = (EquipmentEntity)svs.getLinkedEquipmentData();
                        if(equipmentEntity != null)
                        {
                            equipmentEntity.setLastRecord(equipmentLastRecordContent);
                            equipmentEntity.setSvsState(lastSvsState);
                        }

                        SVSEntity svsEntity = (SVSEntity)svs.getLinkedSvsData();
                        if(svsEntity != null)
                        {
                            svsEntity.setLastRecord(svsLastRecordContent);
                            svsEntity.setSvsState(lastSvsState);
                        }
                    }
                });

                //성공
                ToastUtil.showShort("Success Save.");
            }

            chartrecordActivity.finish();
            finish();
        }

    }


    private void dialog_Cancel(){

        DialogUtil.yesNo(this,
                "Record",
                "Do you want to cancel the record?",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        confirmCancel();
                    }
                },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }
        );
    }

    private void dialog_Save(){

        DialogUtil.yesNo(this,
                "Record",
                "Do you want to save?",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        save();
                    }
                },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }
        );
    }

}
