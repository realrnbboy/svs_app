package kr.co.signallink.svsv2.views.custom;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.services.MyApplication;
import kr.co.signallink.svsv2.utils.StringUtil;
import kr.co.signallink.svsv2.utils.ValueUtil;

/*
    MakeBy.shseo

    버튼이 리스트 형태로 존재하는 다이얼로그.
    생성자를 통해서 기본이 되는 레이아웃을 입력받음.
    내부 스크롤은 생기지 않음.
    버튼의 높이가 화면을 넘을 경우 재조정됨.
 */

public class CustomListDialog extends Dialog implements View.OnClickListener {

    //LayoutIds
    private final int dialogLayoutId = R.layout.custom_list_dialog;
    private final int commonButtonLayoutId = R.layout.custom_list_dialog_element_common_button;
    private final int cancelButtonLayoutId = R.layout.custom_list_dialog_element_cancel_button;

    //Buttons
    private ArrayList<Button> commonButtons = new ArrayList<>();
    private Button cancelButton;

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
    public CustomListDialog(Context context, int stringArrayId) {
        super(context);

        //화면 초기화
        initView();

        //버튼 초기화
        initButtons(StringUtil.getStringArray(stringArrayId));
    }

    @Override
    public void onClick(View v) {

        //취소 버튼을 눌렀을때 처리
        if(v.equals(cancelButton))
        {
            onCustomListButtonDialogListener.onBtnCancelClicked();
        }
        //이외의 버튼을 눌렀을때 처리
        else
        {
            Integer integer = (Integer)v.getTag();
            int buttonIdx = integer.intValue();
            onCustomListButtonDialogListener.onBtnClicked(buttonIdx);
        }
        dismiss();
    }



    //화면 초기화
    private void initView() {

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

    //버튼 초기화
    private void initButtons(String[] buttonNames) {

        if(buttonNames == null || buttonNames.length == 0)
        {
            return;
        }

        //리스트 뷰
        LinearLayout buttonContainer = findViewById(R.id.list);
        if(buttonContainer == null)
        {
            return;
        }

        //인플레이터 세팅
        LayoutInflater inflater = LayoutInflater.from(MyApplication.getInstance().getAppContext());

        //추가할 버튼 갯수
        int buttonCount = buttonNames.length;

        //하위 버튼 리스너 연결 및 태그 추가
        for(int i=0; i<buttonCount; i++)
        {
            Button button = (Button)inflater.inflate(commonButtonLayoutId, null);
            button.setText(buttonNames[i]);
            button.setOnClickListener(this);
            button.setTag(new Integer(i+1));
            commonButtons.add(button);

            buttonContainer.addView(button);
        }

        //취소 버튼 추가
        cancelButton = (Button)inflater.inflate(cancelButtonLayoutId, null);
        cancelButton.setText("CANCEL");
        cancelButton.setOnClickListener(this);
        buttonContainer.addView(cancelButton);

        //버튼 사이즈 체크
        checkButtonHeight();
    }

    //버튼 사이즈 재조정
    private void checkButtonHeight()
    {
        //버튼의 총 갯수를 통해서 사이즈 측정
        final int height = ValueUtil.getScreenHeight();
        final int canDisplayHeight = height - (ValueUtil.convertDpToPx(15) * 2);

        //버튼들의 전체 높이 측정
        int buttonTotalHeight = cancelButton.getHeight();
        for(Button button : commonButtons){
            buttonTotalHeight += button.getHeight();
        }

        //출력 가능한 사이즈보다 버튼의 크기가 크다면, 조절하기
        if(canDisplayHeight < buttonTotalHeight)
        {
            final int buttonCount = commonButtons.size() + 1; //리스트 버튼 들 + 캔슬버튼
            final int buttonNewHeight = canDisplayHeight / buttonCount;

            //사이즈 적용
            for(Button button : commonButtons){
                button.setHeight(buttonNewHeight);
            }
            cancelButton.setHeight(buttonNewHeight);
        }
    }

}
