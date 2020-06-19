package kr.co.signallink.svsv2.views.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.dto.AnalysisData;
import kr.co.signallink.svsv2.model.DiagnosisImageModel;
import kr.co.signallink.svsv2.model.VARIABLES_1_Type;
import kr.co.signallink.svsv2.server.SendPost;
import kr.co.signallink.svsv2.services.SendMessageHandler;
import kr.co.signallink.svsv2.utils.DialogUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.utils.Utils;

// added by hslee 2020.06.19
// 이미지 확대 표시를 위해 사용
public class ImageDetailActivity extends BaseActivity {

    private static final String TAG = "ImageDetailActivity";
    String fileName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        fileName = getIntent().getStringExtra("fileName");

        Toolbar svsToolbar = findViewById(R.id.toolbar);

        setSupportActionBar(svsToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        findViewById(R.id.button_record).setVisibility(View.GONE);  // 안쓰는 버튼 숨김

        AssetManager am = getResources().getAssets() ;
        InputStream is = null ;

        try {
            // 애셋 폴더에 저장된 field.png 열기.
            is = am.open(fileName) ;

            // 입력스트림 is를 통해 field.png 을 Bitmap 객체로 변환.
            Bitmap bm = BitmapFactory.decodeStream(is) ;

            // 만들어진 Bitmap 객체를 이미지뷰에 표시.
            PhotoView photoView = findViewById(R.id.photoView);
            photoView.setImageBitmap(bm);

            PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(photoView);

            // 화면에 꽉차는 옵션 (선택사항)
            //photoViewAttacher.setScaleType(ImageView.ScaleType.FIT_XY);

            photoViewAttacher.update(); // 줌 기능 업데이트

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
    }

    @Override
    public void onResume() {
        super.onResume();
        DefLog.d(TAG, "onResume");
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
