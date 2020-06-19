package kr.co.signallink.svsv2.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.io.InputStream;
import java.util.ArrayList;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.model.DiagnosisImageModel;
import kr.co.signallink.svsv2.views.activities.ImageDetailActivity;


/**
 * Created by hslee on 2020.04.06
 */

public class ResultDiagnosisImageAdapter extends ArrayAdapter<DiagnosisImageModel> {
    private Context context;
    private int itemLayoutResource;
    int listviewSize;


    public ResultDiagnosisImageAdapter(Context context, int itemLayoutResource, ArrayList<DiagnosisImageModel> diagnosisImageModel) {
        super(context, itemLayoutResource, diagnosisImageModel);
        this.itemLayoutResource = itemLayoutResource;
        this.context = context;
        listviewSize = diagnosisImageModel.size();
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(this.itemLayoutResource, null);
        }


        AssetManager am = view.getResources().getAssets() ;
        InputStream is = null ;

        final DiagnosisImageModel diagnosisImageModel = getItem(position);

        try {
            // 애셋 폴더에 저장된 field.png 열기.
            is = am.open(diagnosisImageModel.getFileName()) ;

            // 입력스트림 is를 통해 field.png 을 Bitmap 객체로 변환.
            Bitmap bm = BitmapFactory.decodeStream(is) ;

            // 만들어진 Bitmap 객체를 이미지뷰에 표시.
            ImageView ImageViewDiagnosis = view.findViewById(R.id.ImageViewDiagnosis);
            ImageViewDiagnosis.setImageBitmap(bm);

            if( listviewSize == position + 1 ) { // 마지막 아이템이면 아래 마진 추가
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, 200);
                ImageViewDiagnosis.setLayoutParams(params);
            }

            ImageViewDiagnosis.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // 이미지 상세보기 화면으로 이동
                    Intent intent = new Intent(getContext(), ImageDetailActivity.class);
                    intent.putExtra("fileName", diagnosisImageModel.getFileName());
                    getContext().startActivity(intent);
                }
            });

            is.close() ;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (is != null) {
            try {
                is.close() ;
            } catch (Exception e) {
                e.printStackTrace() ;
            }
        }

        return view;
    }
}