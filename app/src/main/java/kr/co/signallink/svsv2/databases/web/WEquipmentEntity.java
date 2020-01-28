package kr.co.signallink.svsv2.databases.web;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class WEquipmentEntity extends RealmObject {

    @PrimaryKey
    public String id;

    public String name;
    public String company_name;
    public String factory_name;
    public String picture;
    public String created;

    //parents
    public String company_id;
    public String factory_id;

    @Ignore
    final String PARAM_ID = "id";
    @Ignore
    final String PARAM_NAME = "name";
    @Ignore
    final String PARAM_COMPANY_NAME = "company_name";
    @Ignore
    final String PARAM_FACTORY_NAME = "factory_name";
    @Ignore
    final String PARAM_PICTURE = "picture";
    @Ignore
    final String PARAM_CREATED = "created";
    @Ignore
    final String PARAM_FACTORY_ID = "factory_id";


    public boolean putJson(JSONObject userJsonObject)
    {
        if(userJsonObject == null){
            return false;
        }
        if(!userJsonObject.has(PARAM_ID)){
            return false;
        }
        else if(!userJsonObject.has(PARAM_NAME)){
            return false;
        }
        else if(!userJsonObject.has(PARAM_COMPANY_NAME)){
            return false;
        }
        else if(!userJsonObject.has(PARAM_FACTORY_NAME)){
            return false;
        }
        else if(!userJsonObject.has(PARAM_PICTURE)){
            return false;
        }
        else if(!userJsonObject.has(PARAM_CREATED)){
            return false;
        }
        else if(!userJsonObject.has(PARAM_FACTORY_ID)){
            return false;
        }

        try {
            id = userJsonObject.getString(PARAM_ID);
            name = userJsonObject.getString(PARAM_NAME);
            company_name = userJsonObject.getString(PARAM_COMPANY_NAME);
            factory_name = userJsonObject.getString(PARAM_FACTORY_NAME);
            picture = userJsonObject.getString(PARAM_PICTURE);
            created = userJsonObject.getString(PARAM_CREATED);
            factory_id = userJsonObject.getString(PARAM_FACTORY_ID);

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }


        return true;
    }
}
