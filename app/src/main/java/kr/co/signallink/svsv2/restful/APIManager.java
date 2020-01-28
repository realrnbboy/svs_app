package kr.co.signallink.svsv2.restful;

import java.util.List;

import kr.co.signallink.svsv2.restful.request.CompanyAPIRequest;
import kr.co.signallink.svsv2.restful.request.EquipmentAPIRequest;
import kr.co.signallink.svsv2.restful.request.LoginAPIRequest;
import kr.co.signallink.svsv2.restful.request.SvsAPIRequest;
import kr.co.signallink.svsv2.restful.response.APIResponse;
import kr.co.signallink.svsv2.restful.response.AuthLoginResponse;
import kr.co.signallink.svsv2.restful.response.CompanyGetResponse;
import kr.co.signallink.svsv2.restful.response.EquipmentGetListResponse;
import kr.co.signallink.svsv2.restful.response.SvsGetListResponse;
import kr.co.signallink.svsv2.utils.StringUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class APIManager {

    private static APIManager apiManager = null;
    private APIInterface apiInterface;

    public static APIManager getInstance(){
        if(apiManager == null){
            apiManager = new APIManager();
        }

        return apiManager;
    }

    public APIManager(){

        //네트워크 요청 초기화
        apiInterface = APIClient.getClient().create(APIInterface.class);
    }



    //////////////////////////////////////////////////

    //로그인
    public void login(String id, String password, final OnApiListener onApiListener)
    {
        LoginAPIRequest loginRequest = new LoginAPIRequest(id, password);
        Call<AuthLoginResponse> call = apiInterface.login(loginRequest.getRequestBody());
        call.enqueue(new Callback<AuthLoginResponse>() {
            @Override
            public void onResponse(Call<AuthLoginResponse> call, Response<AuthLoginResponse> response) {

                AuthLoginResponse body = response.body();
                if(body.error != 0)
                {
                    if(body.error == 1)
                    {
                        showFailMessage(onApiListener, "Please check your id or password.");
                    }
                    else
                    {
                        showFailMessage(onApiListener, "err"+body.error+"."+body.message);
                    }
                }
                else
                {
                    if(body.user == null)
                    {
                        showFailMessage(onApiListener, "Fatal(1):"+"Wrong User Data.");
                    }
                    else
                    {
                        AuthLoginResponse.Datum user = body.user;
                        if(StringUtil.isEmpty(user.id) || StringUtil.isEmpty(user.company_id) || StringUtil.isEmpty(user.email) || StringUtil.isEmpty(user.full_name))
                        {
                            showFailMessage(onApiListener, "Fatal(2):"+"Wrong User Data.");
                        }
                        else
                        {
                            //Success
                            getCompany(user.company_id, onApiListener);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<AuthLoginResponse> call, Throwable t) {
                showFailMessage(onApiListener,  "Please check your internet connection. Please try again.");
            }
        });
    }

    //로그아웃
    public void logout(final OnApiListener onApiListener)
    {
        Call<APIResponse> call = apiInterface.logout();
        call.enqueue(new Callback<APIResponse>() {
            @Override
            public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {

                APIResponse body = response.body();
                if(body.error != 0)
                {
                    showFailMessage(onApiListener, "err"+body.error+"."+body.message);
                }
                else
                {
                    onApiListener.success(body);
                }
            }

            @Override
            public void onFailure(Call<APIResponse> call, Throwable t) {
                showFailMessage(onApiListener,  "Please check your internet connection. Please try again.");
            }
        });
    }

    //고객사 정보 가져오기
    public void getCompany(String company_id, final OnApiListener onApiListener)
    {
        CompanyAPIRequest companyAPIRequest = new CompanyAPIRequest(company_id);
        Call<CompanyGetResponse> call = apiInterface.getCompany(companyAPIRequest.getRequestBody());
        call.enqueue(new Callback<CompanyGetResponse>() {
            @Override
            public void onResponse(Call<CompanyGetResponse> call, Response<CompanyGetResponse> response) {

                CompanyGetResponse body = response.body();
                if(body.error != 0)
                {
                    showFailMessage(onApiListener, "err"+body.error+"."+body.message);
                }
                else
                {
                    CompanyGetResponse.Datum item = body.item;

                    if(item == null)
                    {
                        showFailMessage(onApiListener, "Fatal(1):"+"Wrong User Data.");
                    }
                    else
                    {
                        if(StringUtil.isEmpty(item.id) || StringUtil.isEmpty(item.name) || StringUtil.isEmpty(item.created))
                        {
                            showFailMessage(onApiListener, "Fatal(2):"+"Wrong User Data.");
                        }
                        else
                        {
                            //Success
                            onApiListener.success(body);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<CompanyGetResponse> call, Throwable t) {
                showFailMessage(onApiListener,  "Please check your internet connection. Please try again.");
            }
        });
    }

    //설비목록 조회
    public void getEquipmentList(String company_id, final OnApiListener onApiListener)
    {
        EquipmentAPIRequest equipmentAPIRequest = new EquipmentAPIRequest(company_id);
        Call<EquipmentGetListResponse> call = apiInterface.getEquipmentList(equipmentAPIRequest.getRequestBody());
        call.enqueue(new Callback<EquipmentGetListResponse>() {
            @Override
            public void onResponse(Call<EquipmentGetListResponse> call, Response<EquipmentGetListResponse> response) {

                EquipmentGetListResponse body = response.body();
                if(body.error != null && body.error != 0)
                {
                    showFailMessage(onApiListener, "err"+body.error+"."+body.message);
                }
                else
                {
                    List<EquipmentGetListResponse.Datum> list = body.list;

                    if(list == null)
                    {
                        showFailMessage(onApiListener, "Fatal(1):"+"Wrong User Data.");
                    }
                    else
                    {
                        //Success
                        onApiListener.success(body);
                    }
                }
            }

            @Override
            public void onFailure(Call<EquipmentGetListResponse> call, Throwable t) {
                showFailMessage(onApiListener,  "Please check your internet connection. Please try again.");
            }
        });

    }

    //센서 목록 조회
    public void getSvsList(int currentPage, String company_id, String equipment_id, final OnApiListener onApiListener)
    {
        SvsAPIRequest svsAPIRequest = new SvsAPIRequest(currentPage+"", company_id, equipment_id);
        Call<SvsGetListResponse> call = apiInterface.getSvsList(svsAPIRequest.getRequestBody());
        call.enqueue(new Callback<SvsGetListResponse>() {
            @Override
            public void onResponse(Call<SvsGetListResponse> call, Response<SvsGetListResponse> response) {

                SvsGetListResponse body = response.body();
                if(body.error != null && body.error != 0)
                {
                    showFailMessage(onApiListener, "err"+body.error+"."+body.message);
                }
                else
                {
                    List<SvsGetListResponse.Datum> list = body.list;

                    if(list == null)
                    {
                        showFailMessage(onApiListener, "Fatal(1):"+"Wrong User Data.");
                    }
                    else
                    {
                        //Success
                        onApiListener.success(body);
                    }
                }
            }

            @Override
            public void onFailure(Call<SvsGetListResponse> call, Throwable t) {
                showFailMessage(onApiListener,  "Please check your internet connection. Please try again.");
            }
        });
    }











































    //리스너를 통해 메세지 전달(true), 또는 토스트 출력(false)
    public void showFailMessage(OnApiListener apiListener, String message) {

        if(apiListener != null)
        {
            boolean hideToast = apiListener.fail(message);
            if(!hideToast){
                ToastUtil.showShort(message);
            }
        }
        else
        {
            ToastUtil.showShort(message);
        }
    }




}
