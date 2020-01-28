package kr.co.signallink.svsv2.databases;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;
import kr.co.signallink.svsv2.utils.StringUtil;

public class SVSWebEntity extends RealmObject {

    @PrimaryKey
    public String uuid;

    public String lastUrl;
}
