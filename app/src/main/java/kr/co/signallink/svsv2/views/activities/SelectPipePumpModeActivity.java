package kr.co.signallink.svsv2.views.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.fivehundredpx.android.blur.BlurringView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import io.realm.RealmResults;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.databases.dao.RealmDao;
import kr.co.signallink.svsv2.databases.web.WebLoginEntity;
import kr.co.signallink.svsv2.services.UartService;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.DialogUtil;
import kr.co.signallink.svsv2.utils.FileUtil;
import kr.co.signallink.svsv2.utils.SharedUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.utils.Utils;
import kr.pe.burt.android.lib.animategradientview.AnimateGradientView;

import static java.lang.System.exit;

// added by hslee 2020.04.27
// 배관, 펌프 선택 화면
// 기존 코드 그대로 이용
public class SelectPipePumpModeActivity extends BaseActivity {

    private ViewGroup llRoot;
    private BlurringView llMode1, llMode2;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_pipe_pump_new);

//        llRoot = findViewById(R.id.llRoot);
//        llMode1 = findViewById(R.id.llMode1);
//        llMode2 = findViewById(R.id.llMode2);
//
//        //그라디언트 뷰
//        AnimateGradientView agv = findViewById(R.id.agv);
//
//        //블러효과
//        llMode1.setBlurredView(llRoot);
//        llMode2.setBlurredView(llRoot);

    }

    @Override
    public void onBackPressed() {

        DialogUtil.closeApp(this,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //앱 종료시 블루투스 연결 끄기
                        UartService uartService = SVS.getInstance().getUartService();
                        if (uartService != null) {
                            uartService.disconnect();
                        }

                        //앱 종료
                        exit(0);
                    }
                },
                null
        );

    }

    //Pipe 클릭
    public void mode1(View view) {

        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.putExtra("pipePumpMode", "pipe");
        startActivity(intent);

        SVS.getInstance().setScreenMode(DefConstant.SCREEN_MODE.LOCAL);
    }


    //pump 클릭
    public void mode2(View view) {

        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.putExtra("pipePumpMode", "pump");
        startActivity(intent);

        SVS.getInstance().setScreenMode(DefConstant.SCREEN_MODE.LOCAL);
    }

    //pump 클릭
    public void manual(View view) { // added by hslee 2020.07.16

        AssetManager assetManager = getAssets();

        try {
            String fileName = "manual.pdf";
            InputStream in = assetManager.open(fileName);   // assets 폴더의 원본 파일 설정
            File file = new File(SVS.rootDir, fileName); // 저장할 파일 설정
            OutputStream out = new FileOutputStream(SVS.rootDir+fileName);

            // 내부저장소에 저장하는 방식 - not work
            //File file = new File(getFilesDir(), fileName); // 저장할 파일 설정
            //OutputStream out = openFileOutput(file.getName(), Context.MODE_PRIVATE);    // output stream 설정

            Utils.copyFile(in, out);    // 파일 복사

            in.close();
            out.flush();
            out.close();

            if( !file.exists() ) {
                ToastUtil.showShort("manual file deleted or error.");
                return;
            }

            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();    // for pdf open
            StrictMode.setVmPolicy(builder.build());

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType( Uri.fromFile(file), "application/pdf");

            // 내부저장소에 저장하는 방식 - not work
            //intent.setDataAndType( Uri.parse("file//" + getFilesDir() + fileName), "application/pdf");

            startActivity(intent);
        }
        catch (ActivityNotFoundException e) {
            ToastUtil.showShort("please install pdf viewer app.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //버전 초기화
        initAppVersion();

        //모드 이동 자동인지 확인
        if(!SharedUtil.load(SharedUtil.KEY.AUTO_NEXT_GO_MAIN.toString(), "").equals(""))
        {
            //로그인이 확인 되었다면, 메인페이지로 이동
            RealmResults<WebLoginEntity> webLoginEntities = new RealmDao<WebLoginEntity>(WebLoginEntity.class).loadAll();
            if (webLoginEntities.size() > 0)
            {
                WebLoginEntity webLoginEntity = webLoginEntities.get(0);

                goIntent(MainActivity.class, true);
            }
        }
    }

    //버전 초기화
    private void initAppVersion()
    {
        TextView appVersion = findViewById(R.id.appversion);
        String verInfo = String.format("SW Ver. -");
        try {
            PackageInfo i = getPackageManager().getPackageInfo(getPackageName(), 0);
            verInfo = String.format("SW Ver. %s", i.versionName);
        } catch(PackageManager.NameNotFoundException e) { }
        appVersion.setText(verInfo);
    }
}