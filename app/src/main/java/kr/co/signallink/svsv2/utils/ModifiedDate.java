package kr.co.signallink.svsv2.utils;

import java.io.File;
import java.util.Comparator;

import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.commons.DefFile;

public class ModifiedDate implements Comparator
{
    int order = DefConstant.ASCENDING_ORDER;
    public ModifiedDate(int order) {
        this.order = order;
    }

    public int compare(Object o1, Object o2)
    {
        String stro1 = (String)o1 + File.separator + DefFile.NAME.CREAT_DIR;
        String stro2 = (String)o2 + File.separator + DefFile.NAME.CREAT_DIR;
        File f1 = new File(stro1);
        File f2 = new File(stro2);

        if (f1.lastModified() > f2.lastModified())
            return order;

        if (f1.lastModified() == f2.lastModified())
            return 0;

        return order*(int)(-1);
    }
}
