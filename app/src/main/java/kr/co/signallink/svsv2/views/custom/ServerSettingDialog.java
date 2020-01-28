package kr.co.signallink.svsv2.views.custom;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.utils.SharedUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.utils.ValueUtil;

import static kr.co.signallink.svsv2.utils.SharedUtil.KEY.CONNECT_SERVER_IP;
import static kr.co.signallink.svsv2.utils.SharedUtil.KEY.CONNECT_SERVER_PORT;

/*
    MakeBy.shseo

    서버의 ip와 port를 설정하는 구조
 */

public class ServerSettingDialog extends Dialog implements View.OnClickListener {

    //LayoutIds
    private final int dialogLayoutId = R.layout.server_setting_dialog;

    //Edit
    private EditText etServerIp, etServerPort;

    //Buttons
    private Button btnReset, btnReset2;
    private Button btnCancel, btnConfirm;

    //Values
    private String serverIp = DefConstant.DEFAULT_SERVER_IP, serverPort = DefConstant.DEFAULT_SERVER_PORT;



    //리스너
    private OnCustomListButtonDialogListener onCustomListButtonDialogListener;
    public interface OnCustomListButtonDialogListener {
        void onBtnClicked(int viewIdx);
        void onBtnCancelClicked();
    }
    public void setDialogListener(OnCustomListButtonDialogListener onCustomListButtonDialogListener){
        this.onCustomListButtonDialogListener = onCustomListButtonDialogListener;
    }

    //생성자
    public ServerSettingDialog(Context context) {
        super(context);

        //값 초기화
        serverIp = SharedUtil.load(CONNECT_SERVER_IP.toString(), DefConstant.DEFAULT_SERVER_IP);
        serverPort = SharedUtil.load(CONNECT_SERVER_PORT.toString(), DefConstant.DEFAULT_SERVER_PORT);


        //화면 초기화
        initWindow();

        //버튼 초기화
        initViews();
    }

    @Override
    public void onClick(View v) {

        boolean canDismiss = true;

        //취소 버튼을 눌렀을때 처리
        if(v.equals(btnCancel))
        {
            onCustomListButtonDialogListener.onBtnCancelClicked();
        }
        //이외의 버튼을 눌렀을때 처리
        else
        {
            //적혀 있는 값 저장하기
            boolean ret = saveValues();

            if(ret){
                Integer integer = (Integer)v.getTag();
                int buttonIdx = integer.intValue();
                onCustomListButtonDialogListener.onBtnClicked(buttonIdx);
            }

            canDismiss = ret;
        }

        if(canDismiss){
            dismiss();
        }
    }



    //화면 초기화
    private void initWindow() {

        //다이얼로그의 타이틀 바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //레이아웃 세팅
        this.setContentView(dialogLayoutId);

        //다이얼로그의 배경을 투명으로 만듭니다.
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //사이즈를 전체 창 사이즈로 확대
        WindowManager.LayoutParams wm = this.getWindow().getAttributes();
        wm.copyFrom(this.getWindow().getAttributes());
        wm.width = ValueUtil.getScreenWidth();
        wm.height = ValueUtil.getScreenHeight();
    }

    //뷰 초기화
    private void initViews() {

        //EditText
        etServerIp = findViewById(R.id.etServerIp);
        etServerIp.setText(serverIp);
        etServerPort = findViewById(R.id.etServerPort);
        etServerPort.setText(serverPort);

        //초기화 버튼
        btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                etServerIp.setText(DefConstant.DEFAULT_SERVER_IP);
                etServerPort.setText(DefConstant.DEFAULT_SERVER_PORT);
            }
        });

        btnReset2 = findViewById(R.id.btnReset2);
        btnReset2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                etServerIp.setText("192.168.0.163");
                etServerPort.setText("12030");
            }
        });


        //취소 버튼 추가
        btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setTag(0);
        btnCancel.setOnClickListener(this);

        //확인 버튼 추가
        btnConfirm = findViewById(R.id.btnConfirm);
        btnConfirm.setTag(1);
        btnConfirm.setOnClickListener(this);
    }

    //값 저장
    private boolean saveValues() {

        serverIp = etServerIp.getText().toString();
        serverPort = etServerPort.getText().toString();

        //check
        if(serverIp.length() >= 3 && serverPort.length() >= 1)
        {
            //적혀 있는 값 저장하기
            SharedUtil.save(CONNECT_SERVER_IP.toString(), serverIp);
            SharedUtil.save(CONNECT_SERVER_PORT.toString(), serverPort);

            return true;
        }
        else
        {
            ToastUtil.showShort("Invalid Value.");
            return false;
        }


    }

}
