package kr.co.signallink.svsv2.databases.web;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.annotations.Required;

public class WCompanyEntity extends RealmObject {

    @PrimaryKey
    public String id;

    public String name;
    public String created;

    @Ignore
    final String PARAM_COMPANY_ID = "company_id";
    @Ignore
    final String PARAM_NAME = "name";
    @Ignore
    final String PARAM_CREATED = "created";


    public boolean putJson(JSONObject userJsonObject)
    {
        if(userJsonObject == null){
            return false;
        }
        if(!userJsonObject.has(PARAM_COMPANY_ID)){
            return false;
        }
        else if(!userJsonObject.has(PARAM_NAME)){
            return false;
        }
        else if(!userJsonObject.has(PARAM_CREATED)){
            return false;
        }

        try {
            id = userJsonObject.getString(PARAM_COMPANY_ID);
            name = userJsonObject.getString(PARAM_NAME);
            created = userJsonObject.getString(PARAM_CREATED);

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }


        return true;
    }


}
