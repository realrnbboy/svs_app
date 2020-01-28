package kr.co.signallink.svsv2.views.activities;

import android.app.Activity;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;

import io.realm.Realm;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.databases.DatabaseUtil;
import kr.co.signallink.svsv2.databases.web.WebLoginEntity;
import kr.co.signallink.svsv2.restful.APIManager;
import kr.co.signallink.svsv2.restful.OnApiListener;
import kr.co.signallink.svsv2.restful.response.APIResponse;
import kr.co.signallink.svsv2.restful.response.CompanyGetResponse;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.AESUtil;
import kr.co.signallink.svsv2.utils.IntentUtil;
import kr.co.signallink.svsv2.utils.KeyboardUtil;
import kr.co.signallink.svsv2.utils.SharedUtil;
import kr.co.signallink.svsv2.utils.StringUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;

import static kr.co.signallink.svsv2.commons.DefConstant.WEB_URL;

public class LoginActivity extends Activity {

    private TextInputEditText etLoginId, etLoginPassword;
    private Button btnLogin;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initCloseButton();
        initEditText();
        initLoginButton();
        buttonAddLink(R.id.btnSignIn, WEB_URL+"/user/signup");
        buttonAddLink(R.id.btnForgotId, WEB_URL+"/user/find/1");
        buttonAddLink(R.id.btnForgotPassword, WEB_URL+"/user/find/2");

        //키보드 활성화
        KeyboardUtil.showKeyboard();
    }

    private void initCloseButton()
    {
        ImageButton btn = findViewById(R.id.btnClose);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //키보드 닫기
                KeyboardUtil.closeKeyboard();

                finish();
            }
        });
    }

    private void initEditText()
    {
        etLoginId = findViewById(R.id.etLoginId);
        etLoginId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkLoginInfo();
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkLoginInfo();
            }
        });
        etLoginId.requestFocus(); //포커스 주기

        etLoginPassword = findViewById(R.id.etLoginPassword);
        etLoginPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkLoginInfo();
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkLoginInfo();
            }
        });

    }

    private void initLoginButton()
    {
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                login();
            }
        });
    }

    private void buttonAddLink(int id, final String url)
    {
        Button btn = findViewById(id);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentUtil.goActivity(LoginActivity.this, WebActivity.class, WebActivity.TARGET_URL, url);
            }
        });
    }


    //텍스트 입력 검사
    private void checkLoginInfo(){

        String id = etLoginId.getText().toString();
        String pw = etLoginPassword.getText().toString();

        if(StringUtil.isEmpty(id) || StringUtil.isEmpty(pw))
        {
            btnLogin.setEnabled(false);
        }
        else
        {
            btnLogin.setEnabled(true);
        }


    }



    //로그인
    private void login()
    {
        final String id = etLoginId.getText().toString();
        final String pw = etLoginPassword.getText().toString();

        if(StringUtil.isEmpty(id) || StringUtil.isEmpty(pw))
        {
            ToastUtil.showShort("Please enter your ID or Password.");
            return;
        }
        else
        {
            APIManager.getInstance().login(id, pw, new OnApiListener() {
                @Override
                public void success(APIResponse apiResponse) {

                    final CompanyGetResponse companyGetResponse = (CompanyGetResponse)apiResponse;


                    DatabaseUtil.transaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {

                            AESUtil aesUtil = new AESUtil(DefConstant.보안키);

                            WebLoginEntity webLoginEntity = new WebLoginEntity();
                            webLoginEntity.id = aesUtil.encrypt(id);
                            webLoginEntity.password = aesUtil.encrypt(pw);
                            webLoginEntity.full_name = companyGetResponse.item.name;
                            webLoginEntity.company_id = companyGetResponse.item.id;
                            webLoginEntity.secured = true;

                            realm.copyToRealm(webLoginEntity);
                        }
                    });


                    //자동 로그인 모드 켜기
                    SharedUtil.save(SharedUtil.KEY.AUTO_NEXT_GO_MAIN.toString(), "1");

                    //웹 모드로 변경하기
                    SVS.getInstance().setScreenMode(DefConstant.SCREEN_MODE.WEB);

                    //로그인 성공 메세지
                    ToastUtil.showShort("You have successfully logged in.");

                    //키보드 닫기
                    KeyboardUtil.closeKeyboard();

                    //이전 페이지로 이동
                    finish();
                }

                @Override
                public boolean fail(String message) {

                    ToastUtil.showShort(message);

                    return true;
                }
            });
        }

    }

    //
}
