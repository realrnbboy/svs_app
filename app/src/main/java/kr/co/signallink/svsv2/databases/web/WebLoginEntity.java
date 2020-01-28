package kr.co.signallink.svsv2.databases.web;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class WebLoginEntity extends RealmObject {

    public String id;
    public String password;

    public String full_name;
    public String company_id;
    public boolean secured;

    @Ignore
    final String PARAM_FULL_NAME = "full_name";
    @Ignore
    final String PARAM_COMPANY_ID = "company_id";


    public boolean putJson(JSONObject userJsonObject)
    {
        if(userJsonObject == null){
            return false;
        }
        if(!userJsonObject.has(PARAM_FULL_NAME)){
            return false;
        }
        else if(!userJsonObject.has(PARAM_COMPANY_ID)){
            return false;
        }

        try {
            full_name = userJsonObject.getString(PARAM_FULL_NAME);
            company_id = userJsonObject.getString(PARAM_COMPANY_ID);

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }


        return true;
    }

}
