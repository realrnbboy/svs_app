package kr.co.signallink.svsv2.views.activities;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefCMDOffset;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.databases.HistoryEntity;
import kr.co.signallink.svsv2.databases.SVSEntity;
import kr.co.signallink.svsv2.utils.FileUtil;
import kr.co.signallink.svsv2.commons.DefFile;
import kr.co.signallink.svsv2.dto.SVSCode;
import kr.co.signallink.svsv2.dto.SVSParam;
import kr.co.signallink.svsv2.utils.MyValueFormatter;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.DateUtil;
import kr.co.signallink.svsv2.utils.SizeUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;

public class ChartHistoryActivity extends BaseActivity implements OnChartValueSelectedListener {

    public static final String TAG = "ChartHistoryActivity";

    private SVS svs = SVS.getInstance();
    private DefConstant.TrendValue trendType = DefConstant.TrendValue.DPEAK;
    private ArrayList<DefConstant.TrendValue> checkedMeasureDatas = new ArrayList<>();

    private Toolbar svsToolbar;
    private ImageButton btnLeftArrow, btnRightArrow;
    private Spinner datatype_spinner_charthistory;
    private CombinedChart combinedChart = null;
    private TextView tvTrendValue_charthistory;
    private ImageView commentimage_charthistory;
    private EditText comment_charthistory;
    private ArrayAdapter<DefConstant.TrendValue> arrayAdapter;

    private SVSEntity selectedSvsEntity;
    private float selectedIdx = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.charthistory_activity);

        svsToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(svsToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        trendType = DefConstant.TrendValue.DPEAK;
        ((TextView)svsToolbar.findViewById(R.id.toolbar_title)).setText(trendType.toString());
        ((TextView)svsToolbar.findViewById(R.id.toolbar_title)).setText(R.string.history_screen);
        ((Button)findViewById(R.id.button_record)).setVisibility(View.GONE);

        Button button_ble_search = findViewById(R.id.button_control_ble);
        button_ble_search.setText(R.string.history_toolbar_btn_delete);
        button_ble_search.setVisibility(View.GONE);
        button_ble_search.setEnabled(false);
        button_ble_search.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                //삭제 기능
            }
        });

        init();
        getData();

        commentimage_charthistory = findViewById(R.id.commentimage_charthistory);
        comment_charthistory = findViewById(R.id.comment_charthistory);



        arrayAdapter = new ArrayAdapter<DefConstant.TrendValue>(this, R.layout.spinner_item, checkedMeasureDatas);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        datatype_spinner_charthistory = findViewById(R.id.datatype_spinner_charthistory);
        datatype_spinner_charthistory.setAdapter(arrayAdapter);
        datatype_spinner_charthistory.setSelection(trendType.ordinal());
        datatype_spinner_charthistory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?>  parent, View view, int position, long id) {

                DefConstant.TrendValue prevTrendType = trendType;
                trendType = checkedMeasureDatas.get(position);


                //이전 선택과 현재 선택이 다를 경우, TrendValue 불러오기
                if(prevTrendType != trendType)
                {


                    Highlight[] highlights = combinedChart.getHighlighted();
                    if(highlights != null && highlights.length > 0)
                    {
                        //과거 터치
                        float xPx = highlights[0].getX();

                        //그래프 갱신
                        drawChart();

                        //새로 변경된 그래프의 같은 X축 터치
                        Highlight newHighlight = combinedChart.getHighlightByTouchPoint(xPx, 0);

                        //값 찾기
                        HistoryEntity historyEntity = selectedSvsEntity.getHistoryEntities().get((int)xPx);
                        float value = 0;
                        if(trendType == DefConstant.TrendValue.TEMPERATURE)
                        {
                            //temperature trend
                            try
                            {
                                value = historyEntity.getAveragemeasure().getlTempCurrent();
                            }
                            catch (Exception ex) {
                                return;
                            }
                        }
                        else if(trendType == DefConstant.TrendValue.DPEAK)
                        {
                            try
                            {
                                value = historyEntity.getAveragemeasure().getSvsTime().getdPeak();
                            }
                            catch (Exception ex) {
                                return;
                            }
                        }
                        else if(trendType == DefConstant.TrendValue.DRMS)
                        {
                            try
                            {
                                value = historyEntity.getAveragemeasure().getSvsTime().getdRms();
                            }
                            catch (Exception ex) {
                                return;
                            }
                        }
                        else if(trendType == DefConstant.TrendValue.DCRF)
                        {
                            try
                            {
                                value = historyEntity.getAveragemeasure().getSvsTime().getdCrf();
                            }
                            catch (Exception ex) {
                                return;
                            }
                        }
                        else if(DefConstant.TrendType.PEAKS.hasTrendValue(trendType))
                        {
                            try
                            {
                                int index = trendType.getIndex();
                                value = historyEntity.getAveragemeasure().getSvsFreq()[index].getdPeak();
                            }
                            catch (Exception ex) {
                                return;
                            }
                        }
                        else if(DefConstant.TrendType.BANDS.hasTrendValue(trendType))
                        {
                            try {
                                int index = trendType.getIndex();
                                value = historyEntity.getAveragemeasure().getSvsFreq()[index].getdBnd();
                            }
                            catch (Exception ex) {
                                return;
                            }
                        }


                        //값 적용
                        tvTrendValue_charthistory.setText(""+value);
                    }
                    else
                    {

                        //그래프 갱신
                        drawChart();

                        tvTrendValue_charthistory.setText("");
                    }

                }
            }
            public void onNothingSelected(AdapterView<?>  parent) {
            }
        });

        btnLeftArrow = findViewById(R.id.btnLeftArrow);
        btnLeftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int index = datatype_spinner_charthistory.getSelectedItemPosition() - 1;
                if(index < 0)
                    index = checkedMeasureDatas.size()-1; //loop

                datatype_spinner_charthistory.setSelection(index,true);
                arrayAdapter.notifyDataSetChanged();
            }
        });
        btnRightArrow = findViewById(R.id.btnRightArrow);
        btnRightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int index = datatype_spinner_charthistory.getSelectedItemPosition() + 1;
                if(index >= checkedMeasureDatas.size())
                    index = 0; //loop

                datatype_spinner_charthistory.setSelection(index, true);
                arrayAdapter.notifyDataSetChanged();
            }
        });

        tvTrendValue_charthistory = findViewById(R.id.tvTrendValue_charthistory);

        drawChart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        combinedChart.setUnbindEnabled(true);
        combinedChart = null;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

        selectedIdx = e.getX();

        Log.i("VAL SELECTED","Value: " + e.getY() + ", xIndex: " + selectedIdx + ", DataSet index: " + h.getDataSetIndex());

        Date clickDate = selectedSvsEntity.getHistoryEntities().get((int)selectedIdx).getAveragemeasure().getCaptureTime();

        String strCaptureTime = DateUtil.convertDefaultDetailDate(clickDate);
        String strDate = DateUtil.convertDate(clickDate, "yyyyMMdd");
        String strTime = DateUtil.convertDate(clickDate, "HHmmss");

        String pathDir = DefFile.FOLDER.HISTORY.getFullPath() + selectedSvsEntity.getParentUuid() + File.separator
                + selectedSvsEntity.getUuid() + File.separator
                + strDate + File.separator
                + strTime + File.separator;


        //값
        tvTrendValue_charthistory.setText(""+e.getY());

        //코멘트
        StringBuilder strComment = new StringBuilder();
        try {
            strComment.append(strCaptureTime + "\r\n");
            strComment.append(FileUtil.readComment(pathDir));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        comment_charthistory.setText(strComment);

        //이미지
        String strKeyImagePath = pathDir + DefFile.NAME.RECORD + DefFile.EXT.JPG;
        Glide.with(this).load(strKeyImagePath).placeholder(0).centerCrop().into(commentimage_charthistory);
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    private void init(){
        combinedChart = findViewById(R.id.CombnedChart_charthistory);
        combinedChart.getDescription().setEnabled(false);
        combinedChart.setBackgroundColor(getResources().getColor(R.color.colorContent));
        combinedChart.setOnChartValueSelectedListener(this);
        combinedChart.setOnChartGestureListener(OCGL);
        combinedChart.setMaxVisibleValueCount(20);

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
                //insert xValue
                int a = (int)value + 1;
                return String.valueOf(a);
            }
        });

        xAxis.setAxisMinimum(0);
        xAxis.setGranularity(1.0f);
        xAxis.setTextColor(Color.WHITE);
    }

    private void initCheckedMeasureDatas(int index) {
        checkedMeasureDatas.clear();

        SVSParam svsParam = selectedSvsEntity.getHistoryEntities().get(index).getUploaddata().getSvsParam();
        SVSCode svsCode = svsParam.getCode();

        if(svsCode.getlTempEna() != 0) {
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

    private void getData() {

        //SVS 정보 가져오기
        selectedSvsEntity = (SVSEntity)svs.getSelectedSvsData();
        if(selectedSvsEntity == null)
        {
            ToastUtil.showShort("Not Found SVS Data.");
            return;
        }

        try{
            selectedSvsEntity.getHistoryEntities().clear();

            String historyDir = DefFile.FOLDER.HISTORY.getFullPath();
            String equipmentDir = historyDir + selectedSvsEntity.getParentUuid() + File.separator;
            String svsDir = equipmentDir + selectedSvsEntity.getUuid() + File.separator;


            String strDate;
            String strTime;

            File fSvsDir = new File(svsDir);

            File[] orderedSvsDir = fSvsDir.listFiles();
            FileUtil.fileArraySort(orderedSvsDir);
            for(File fDateDir : orderedSvsDir)
            {
                if(fDateDir.isDirectory())
                {
                    strDate = fDateDir.getName();

                    File[] orderedDateDir = fDateDir.listFiles();
                    FileUtil.fileArraySort(orderedDateDir);
                    for(File fTimeDir : orderedDateDir)
                    {
                        if(fTimeDir.isDirectory())
                        {
                            strTime = fTimeDir.getName();

                            String strCaptureTime = strDate+strTime;
                            Date captureTime = DateUtil.convertString(strCaptureTime, "yyyyMMddhhmmss");
                            String strUploadPath = fTimeDir + File.separator + DefFile.NAME.SETTING + DefFile.EXT.BIN;
                            String strMeasurePath = fTimeDir + File.separator + DefFile.NAME.MEASURE + DefFile.EXT.BIN;

                            HistoryEntity historyData = new HistoryEntity();
                            boolean retReadUpload = FileUtil.readUpload(historyData, captureTime, strUploadPath);
                            boolean retReadMeasure = FileUtil.readMeasure(historyData, captureTime, strMeasurePath);

                            if(retReadUpload && retReadMeasure)
                            {
                                selectedSvsEntity.getHistoryEntities().add(historyData);
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            Log.d("test", "e:"+e.toString());
        }

        if(selectedSvsEntity.getHistoryEntities().size() > 0) {
            initCheckedMeasureDatas(0);
        }
    }

    private void drawChart(){

        ArrayList<HistoryEntity> historyEntities = selectedSvsEntity.getHistoryEntities();
        int historyDataSize = historyEntities.size();
        if(historyDataSize < 1) {
            return;
        }

        ScatterData scatterData = new ScatterData();
        LineData lineData = new LineData();

        XAxis xAxis = combinedChart.getXAxis();
        xAxis.setAxisMaximum(historyDataSize-1);
        xAxis.setLabelRotationAngle(0f); //X축에 있는 라벨의 각도
        xAxis.setLabelCount(5); //X축에 있는 라벨의 갯수 (절대적은 아님)

        ArrayList<Float> valueList = new ArrayList<>();
        ArrayList<Float> valueWarnLis = new ArrayList<>();
        ArrayList<Float> valueDanList = new ArrayList<>();


        if(trendType == DefConstant.TrendValue.TEMPERATURE)
        {
            //temperature trend
            try
            {
                for(HistoryEntity historyEntity : historyEntities)
                {
                    valueList.add((float)historyEntity.getAveragemeasure().getlTempCurrent());
                    valueWarnLis.add((float)historyEntity.getUploaddata().getSvsParam().getlTpWrn());
                    valueDanList.add((float)historyEntity.getUploaddata().getSvsParam().getlTpDan());
                }

                scatterData.addDataSet(generateScatterData(valueList));
            }
            catch (Exception ex) {
                return;
            }
        }
        else if(trendType == DefConstant.TrendValue.DPEAK)
        {
            try
            {
                for(HistoryEntity historyEntity : historyEntities)
                {
                    valueList.add(historyEntity.getAveragemeasure().getSvsTime().getdPeak());
                    valueWarnLis.add(historyEntity.getUploaddata().getSvsParam().getCode().getTimeWrn().getdPeak());
                    valueDanList.add(historyEntity.getUploaddata().getSvsParam().getCode().getTimeDan().getdPeak());
                }

                scatterData.addDataSet(generateScatterData(valueList));
                lineData.addDataSet(generateLineData(true, valueWarnLis));
                lineData.addDataSet(generateLineData(false, valueDanList));
            }
            catch (Exception ex) {
                return;
            }
        }
        else if(trendType == DefConstant.TrendValue.DRMS)
        {
            try
            {
                for(HistoryEntity historyEntity : historyEntities)
                {
                    valueList.add(historyEntity.getAveragemeasure().getSvsTime().getdRms());
                    valueWarnLis.add(historyEntity.getUploaddata().getSvsParam().getCode().getTimeWrn().getdRms());
                    valueDanList.add(historyEntity.getUploaddata().getSvsParam().getCode().getTimeDan().getdRms());
                }

                scatterData.addDataSet(generateScatterData(valueList));
                lineData.addDataSet(generateLineData(true, valueWarnLis));
                lineData.addDataSet(generateLineData(false, valueDanList));
            }
            catch (Exception ex) {
                return;
            }
        }
        else if(trendType == DefConstant.TrendValue.DCRF)
        {
            try
            {
                for(HistoryEntity historyEntity : historyEntities)
                {
                    valueList.add(historyEntity.getAveragemeasure().getSvsTime().getdCrf());
                    valueWarnLis.add(historyEntity.getUploaddata().getSvsParam().getCode().getTimeWrn().getdCrf());
                    valueDanList.add(historyEntity.getUploaddata().getSvsParam().getCode().getTimeDan().getdCrf());
                }

                scatterData.addDataSet(generateScatterData(valueList));
                lineData.addDataSet(generateLineData(true, valueWarnLis));
                lineData.addDataSet(generateLineData(false, valueDanList));
            }
            catch (Exception ex) {
                return;
            }
        }
        else if(DefConstant.TrendType.PEAKS.hasTrendValue(trendType))
        {
            try
            {
                int index = trendType.getIndex();
                for(HistoryEntity historyEntity : historyEntities)
                {
                    valueList.add(historyEntity.getAveragemeasure().getSvsFreq()[index].getdPeak());
                    valueWarnLis.add(historyEntity.getUploaddata().getSvsParam().getCode().getFreqWrn()[index].getdPeak());
                    valueDanList.add(historyEntity.getUploaddata().getSvsParam().getCode().getFreqDan()[index].getdPeak());
                }

                scatterData.addDataSet(generateScatterData(valueList));
                lineData.addDataSet(generateLineData(true, valueWarnLis));
                lineData.addDataSet(generateLineData(false, valueDanList));
            }
            catch (Exception ex) {
                return;
            }
        }
        else if(DefConstant.TrendType.BANDS.hasTrendValue(trendType))
        {
            try {
                int index = trendType.getIndex();
                for(HistoryEntity historyEntity : historyEntities)
                {
                    valueList.add(historyEntity.getAveragemeasure().getSvsFreq()[index].getdBnd());
                    valueWarnLis.add(historyEntity.getUploaddata().getSvsParam().getCode().getFreqWrn()[index].getdBnd());
                    valueDanList.add(historyEntity.getUploaddata().getSvsParam().getCode().getFreqDan()[index].getdBnd());
                }

                scatterData.addDataSet(generateScatterData(valueList));
                lineData.addDataSet(generateLineData(true, valueWarnLis));
                lineData.addDataSet(generateLineData(false, valueDanList));
            }
            catch (Exception ex) {
                return;
            }
        }

        valueList.clear();

        CombinedData combinedData = new CombinedData();
        scatterData.setDrawValues(true);
        lineData.setDrawValues(true);
        combinedData.setData(scatterData);
        combinedData.setData(lineData);

        combinedChart.setData(combinedData);
        //combinedChart.invalidate();

        combinedChart.setSelected(false);

        adjustViewportForChart();
    }

    //이상한 뷰포트 바로 잡기
    private void adjustViewportForChart(){

        //X축 라벨 사이즈의 최대 높이
        Rect rectByText = SizeUtil.getTextRect(""+selectedSvsEntity.getHistoryEntities().size(), combinedChart.getXAxis().getTypeface(), combinedChart.getXAxis().getTextSize());
        float textSize = rectByText.width() + (rectByText.height() * (float)Math.sqrt(2)); //글자의 가로 길이 + 글자를 회전시켰을때 발생되는 최대 사이즈 (height*root(2))

        //레전드의 높이
        float legendSize = combinedChart.getLegend().mNeededHeight + combinedChart.getLegend().getYOffset();

        //뷰포트 offset
        float offsetLeft = combinedChart.getViewPortHandler().offsetLeft();
        float offsetRight = combinedChart.getViewPortHandler().offsetRight();
        float offsetSide = (offsetLeft > offsetRight) ? offsetLeft : offsetRight;
        float offsetBottom = textSize + legendSize;

        combinedChart.setViewPortOffsets(offsetSide,0, offsetSide, offsetBottom);
        combinedChart.invalidate();
    }

    private LineDataSet generateLineData(boolean isWrn, ArrayList<Float> valueList)
    {
        ArrayList<Entry> entries = new ArrayList<>();

        for(int i=0; i<valueList.size(); i++)
        {
            entries.add(new Entry(i, valueList.get(i)));
        }

        LineDataSet lineDataSet = new LineDataSet(entries,isWrn == true? "Warning" : "Danger");
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setValueTextColor(Color.WHITE);
        lineDataSet.setHighlightEnabled(false);
        lineDataSet.setColor(isWrn == true ? Color.YELLOW : Color.RED);

        return lineDataSet;
    }

    private ScatterDataSet generateScatterData(ArrayList<Float> valueList)
    {
        ArrayList<Entry> entries = new ArrayList<>();

        for(int i=0; i<valueList.size(); i++)
        {
            entries.add(new Entry(i, valueList.get(i)));
        }

        ScatterDataSet scatterDataSet = new ScatterDataSet(entries, trendType.getTitle());
        scatterDataSet.setColor(Color.GREEN);
        scatterDataSet.setScatterShapeSize(15f);
        scatterDataSet.setValueTextSize(10f);
        scatterDataSet.setValueTextColor(Color.WHITE);

        scatterDataSet.setValueFormatter(new MyValueFormatter());

        return scatterDataSet;
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
}
