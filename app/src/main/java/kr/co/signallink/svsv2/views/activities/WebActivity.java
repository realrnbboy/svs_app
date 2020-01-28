package kr.co.signallink.svsv2.views.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import im.delight.android.webview.AdvancedWebView;
import io.realm.Realm;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.databases.DatabaseUtil;
import kr.co.signallink.svsv2.databases.SVSWebEntity;
import kr.co.signallink.svsv2.databases.dao.RealmDao;
import kr.co.signallink.svsv2.utils.ToastUtil;

import static kr.co.signallink.svsv2.BuildConfig.DEBUG;

public class WebActivity extends BaseActivity implements AdvancedWebView.Listener {

    public static final String TARGET_UUID = "TARGET_UUID"; //Local모드 일때, Realm DB의 UUID를 가지고 마지막페이지를 조회하는 방법
    public static final String TARGET_URL = "TARGET_URL"; //로드할 페이지 정하기.



    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean bTouchable = true;
    private boolean isMobileMode = true;

    //웹뷰
    private AdvancedWebView mWebView;

    //인터넷 연결 실패용 페이지
    private LinearLayout llCheckGuide;

    //마지막 페이지 기억용 Target
    private String targetUuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        initToolbar();

        //연결 실패용 페이지
        llCheckGuide = findViewById(R.id.llCheckGuide);

        //웹뷰
        mWebView = (AdvancedWebView) findViewById(R.id.webview);
        mWebView.setListener(this, this);

        //스크롤바 항상 보이기
        mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebView.setScrollbarFadingEnabled(false);

        //로딩중에 터치 못하게 변경
        mWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return !bTouchable;
            }
        });

        //페이지 호출
        String targetUrl = (String)getIntent().getSerializableExtra(TARGET_URL);
        if(targetUrl != null)
        {
            goHome(targetUrl);
        }
        else
        {
            targetUuid = (String)getIntent().getSerializableExtra(TARGET_UUID);
            if(targetUuid != null)
            {
                SVSWebEntity loadSvsWebEntity = new RealmDao<SVSWebEntity>(SVSWebEntity.class).loadByUuid(targetUuid);
                if(loadSvsWebEntity == null)
                {
                    //신규로 생성
                    DatabaseUtil.transaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {

                            SVSWebEntity svsWebEntity = new SVSWebEntity();
                            svsWebEntity.uuid = targetUuid;

                            realm.copyToRealmOrUpdate(svsWebEntity);
                        }
                    });

                    goHome(null);
                }
                else
                {
                    goHome(loadSvsWebEntity.lastUrl);
                }
            }
            else
            {
                goHome(null);
            }
        }




        //스와이프 리프레쉬 초기화
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startRefresh();
            }
        });


        //웹뷰의 스크롤이 최상단으로 이동하지 않았는데도 Refresh가 동장하는 문제 수정
        mWebView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {

                mSwipeRefreshLayout.setEnabled(mWebView.getScrollY() == 0);
            }
        });

        //초기 로딩 보여주기...
        mSwipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
        // ...
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        mWebView.onPause();
        // ...
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mWebView.onDestroy();
        // ...
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mWebView.onActivityResult(requestCode, resultCode, intent);
        // ...
    }

    @Override
    public void onBackPressed() {
        if (!mWebView.onBackPressed()) { return; }
        // ...
        super.onBackPressed();
    }


    @Override
    public void onPageStarted(String url, Bitmap favicon) {


    }

    @Override
    public void onPageFinished(final String url) {


        //화면 가리기 - 숨기기
        llCheckGuide.setVisibility(View.GONE);

        //마지막 성공적으로 불러온 페이지 저장
        if(targetUuid != null)
        {
            DatabaseUtil.transaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {

                    SVSWebEntity svsWebEntity = new RealmDao<SVSWebEntity>(SVSWebEntity.class).loadByUuid(targetUuid);
                    if(svsWebEntity != null) {
                        svsWebEntity.lastUrl = url;
                    }
                }
            });
        }

        //클릭 가능
        bTouchable = true;

        // 새로고침 완료
        mSwipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {

        //클릭 가능
        bTouchable = true;

        //화면 가리기 - 보이기
        llCheckGuide.setVisibility(View.VISIBLE);

        // 새로고침 완료
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) { }

    @Override
    public void onExternalPageRequest(String url) { }


    //툴바 초기화
    private void initToolbar(){

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView title_textview = toolbar.findViewById(R.id.toolbar_title);
        title_textview.setText(R.string.title_net_manager);

        Button button_record = findViewById(R.id.button_record);
        button_record.setVisibility(View.GONE);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

    }

    //툴바 옵션 추가
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_webview_menu, menu);

        int size = menu.size();
        for(int i=0; i<size; i++){
            MenuItem menuItem = menu.getItem(i);
            int itemId = menuItem.getItemId();

            if(isMobileMode)
            {
                if(itemId == R.id.menu_web_mobile)
                {
                    menuItem.setVisible(false);
                }
                else
                {
                    menuItem.setVisible(true);
                }
            }
            else
            {
                if(itemId == R.id.menu_web_desktop)
                {
                    menuItem.setVisible(false);
                }
                else
                {
                    menuItem.setVisible(true);
                }
            }

            //릴리즈 모드일땐, 모바일보기 및 데스크탑 보기 버튼 숨기기
            if(!DEBUG){
                if(itemId == R.id.menu_web_mobile || itemId == R.id.menu_web_desktop){
                    menuItem.setVisible(false);
                }
            }
        }

        return true;
    }

    //툴바 옵션 선택
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        switch(itemId)
        {
            case android.R.id.home: //닫기 버튼
                finish();
                break;

            case R.id.menu_web_home:
                goHome(null);
                break;
            case R.id.menu_web_refresh:
                startRefresh();
                break;
            case R.id.menu_web_left:
                goPrev();
                break;
            case R.id.menu_web_right:
                goNext();
                break;
            case R.id.menu_web_mobile:
                showMobileMode();
                break;
            case R.id.menu_web_desktop:
                showDesktopMode();
                break;
        }



        return super.onOptionsItemSelected(item);
    }

    //홈으로 가기
    private void goHome(String url){

        if(url == null)
        {
            //기본
            mWebView.loadUrl(DefConstant.WEB_URL);

            //파일 업로드 테스트
            //mWebView.loadUrl(DefConstant.WEB_FILE_TEST_URL);

            //User-Agent 테스트
            //mWebView.loadUrl("https://httpbin.org/user-agent");

        }
        else
        {
            mWebView.loadUrl(url);
        }

    }

    //새로고침 시작
    private void startRefresh(){

        //클릭 불가능
        bTouchable = false;

        //페이지 갱신
        mWebView.reload();
    }

    //이전으로 가기
    private void goPrev(){
        if(mWebView.canGoBack()){
            mWebView.goBack();
        }
    }

    //다음으로 가기
    private void goNext(){
        if(mWebView.canGoForward()){
            mWebView.goForward();
        }
    }

    //모바일 모드로 보기
    private void showMobileMode(){

        mSwipeRefreshLayout.setRefreshing(true);

        isMobileMode = true;
        mWebView.setDesktopMode(false);
        mWebView.reload();
        invalidateOptionsMenu();
    }

    //데스크탑 모드로 보기
    private void showDesktopMode(){

        mSwipeRefreshLayout.setRefreshing(true);

        isMobileMode = false;
        mWebView.setDesktopMode(true);
        mWebView.reload();
        invalidateOptionsMenu();
    }

}
