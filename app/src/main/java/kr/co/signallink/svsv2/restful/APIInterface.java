package kr.co.signallink.svsv2.restful;

import kr.co.signallink.svsv2.restful.request.CompanyAPIRequest;
import kr.co.signallink.svsv2.restful.request.EquipmentAPIRequest;
import kr.co.signallink.svsv2.restful.request.LoginAPIRequest;
import kr.co.signallink.svsv2.restful.request.SvsAPIRequest;
import kr.co.signallink.svsv2.restful.response.APIResponse;
import kr.co.signallink.svsv2.restful.response.AuthLoginResponse;
import kr.co.signallink.svsv2.restful.response.CompanyGetResponse;
import kr.co.signallink.svsv2.restful.response.EquipmentGetListResponse;
import kr.co.signallink.svsv2.restful.response.SvsGetListResponse;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface APIInterface {


    //로그인
    @POST("auth/app_login")
    Call<AuthLoginResponse> login(@Body RequestBody request);

    //로그아웃
    @POST("auth/app_logout")
    Call<APIResponse> logout();

    //고객사 조회
    @POST("company/app_get")
    Call<CompanyGetResponse> getCompany(@Body RequestBody request);

    //설비 목록 조회
    @POST("equipment/app_get_list")
    Call<EquipmentGetListResponse> getEquipmentList(@Body RequestBody request);

    //센서 목록 조회
    @POST("svs/app_get_list")
    Call<SvsGetListResponse> getSvsList(@Body RequestBody request);


}
