package kr.co.signallink.svsv2.views.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.model.CauseModel;


/**
 * Created by hslee on 2020.01.30
 */

public class ResultDiagnosisListAdapter extends ArrayAdapter<CauseModel> {
    private Context context;
    private int itemLayoutResource;

    Resources resouces = null;

    View mainView;

    public ResultDiagnosisListAdapter(Context context, int itemLayoutResource, ArrayList<CauseModel> causeModel, Resources resouces) {
        super(context, itemLayoutResource, causeModel);
        this.itemLayoutResource = itemLayoutResource;
        this.context = context;
        this.resouces = resouces;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(this.itemLayoutResource, null);
        }

        mainView = view;

        final CauseModel causeModel = getItem(position);

        int progressBarColor;
        int fontColor;
        float fontSizeCause = resouces.getDimension(R.dimen.font_midsmall) / resouces.getDisplayMetrics().density;
        float fontSizeRatio;
        final int LIMIT_RATIO = 70;

        // 비율에 맞게 색상 및 폰트 사이즈 설정
        int ratio = (int)(causeModel.getRatio() * 100);
        //if( ratio > LIMIT_RATIO ) {   // 일정 비율 이상일 때, 빨간색으로 표시할 경우
        if( position == 0 ) {   // 첫 번째만 빨간색으로 표시할 경우
            progressBarColor = ContextCompat.getColor(context, R.color.myred);
            fontColor = ContextCompat.getColor(context, R.color.myred);
            fontSizeRatio = resouces.getDimension(R.dimen.font_larger) / resouces.getDisplayMetrics().density;
        }
        else {
            //progressBarColor = Color.BLUE;
            progressBarColor = ContextCompat.getColor(context, R.color.myBlueLight);
            fontColor = ContextCompat.getColor(context, R.color.myblue);
            fontSizeRatio = resouces.getDimension(R.dimen.font_large) / resouces.getDisplayMetrics().density;
        }

        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView textViewCause = view.findViewById(R.id.textViewCause);
        TextView textViewRatio = view.findViewById(R.id.textViewRatio);

        progressBar.setProgress(ratio);
        progressBar.setProgressTintList(ColorStateList.valueOf(progressBarColor));
        textViewCause.setText(causeModel.getCause());
        textViewCause.setTextColor(fontColor);
        textViewCause.setTextSize(fontSizeCause);
        textViewRatio.setText(ratio + "%");
        textViewRatio.setTextColor(fontColor);
        textViewRatio.setTextSize(fontSizeRatio);

        return view;
    }
}