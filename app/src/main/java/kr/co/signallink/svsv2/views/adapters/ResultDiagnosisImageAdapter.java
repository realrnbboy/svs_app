package kr.co.signallink.svsv2.views.adapters;

import android.content.Context;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.io.InputStream;
import java.util.ArrayList;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.model.DiagnosisImageModel;


/**
 * Created by hslee on 2020.04.06
 */

public class ResultDiagnosisImageAdapter extends ArrayAdapter<DiagnosisImageModel> {
    private Context context;
    private int itemLayoutResource;


    public ResultDiagnosisImageAdapter(Context context, int itemLayoutResource, ArrayList<DiagnosisImageModel> diagnosisImageModel) {
        super(context, itemLayoutResource, diagnosisImageModel);
        this.itemLayoutResource = itemLayoutResource;
        this.context = context;
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
            ImageViewDiagnosis.setImageBitmap(bm) ;

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