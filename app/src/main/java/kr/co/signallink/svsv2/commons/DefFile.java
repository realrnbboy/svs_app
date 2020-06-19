package kr.co.signallink.svsv2.commons;

import java.io.File;
import java.util.ArrayList;

import kr.co.signallink.svsv2.user.SVS;

public class DefFile {

    //폴더 이름
    public enum FOLDER {
        THUMBNAIL("thumbnail"),
        THUMBNAIL_EQUIPMENT("thumbnail", "equipment"),
        THUMBNAIL_SVS("thumbnail","svs"),

        HISTORY("history");

        private String folder;
        private String subFolder;
        private FOLDER(String folder){
            this.folder = folder;
        }
        private FOLDER(String folder, String subFolder){
            this.folder = folder;
            this.subFolder = subFolder;
        }

        public String getFullPath(){

            String path = SVS.getInstance().getRootdir();
            path += folder + File.separator;
            if(subFolder != null) {
                path += subFolder + File.separator;
            }

            return  path;
        }

        @Override
        public String toString() {
            String title = folder;
            if(subFolder != null) {
                title += "_" + subFolder;
            }
            return title;
        }
    }

    //파일 이름
    public enum NAME {

        KEY_IMAGE("key"),
        SVS_IMAGE("svs"),
        REGISTER("register"),
        RECORD("record"),
        COMMENT("comment"),
        MEASURE("measure"),
        SETTING("setting"),
        LAST_RECORD("lastrecord"),
        CREAT_DIR("createdir");

        private String title;
        private NAME(String title){
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    //파일 확장자
    public enum EXT {

        JPG("jpg"),
        CHAR("dat"),
        BIN("mdat");

        private String ext;
        private EXT(String ext) {
            this.ext = ext;
        }

        @Override
        public String toString() {
            return "."+ext;
        }
    }
 

    //폴더 이름
    public enum SVS_LOCATION {

        SVS_1("svs1", 0),
        SVS_2("svs2", 1),
        SVS_3("svs3", 2),
        SVS_4("svs4", 3);

        private String title;
        private int index;
        private SVS_LOCATION(String title, int index){
            this.title = title;
            this.index = index;
        }

        @Override
        public String toString() {
            return title;
        }

        public static SVS_LOCATION getEnumForName(String name){
            for(SVS_LOCATION SVSLOCATION : SVS_LOCATION.values()){
                if(SVSLOCATION.name().equals(name)){
                    return SVSLOCATION;
                }
            }
            return null;
        }

        public static SVS_LOCATION findDIR_forPHOTO(int index){
            for(SVS_LOCATION svsLocation : SVS_LOCATION.values()){
                if(svsLocation.toString().contains(NAME.SVS_IMAGE.toString()) && svsLocation.index == index){
                    return svsLocation;
                }
            }

            return null;
        }

        public static SVS_LOCATION findDIR_forPHOTO(String name){

            for(SVS_LOCATION svsLocation : SVS_LOCATION.values())
            {
                String photo = NAME.SVS_IMAGE.toString();
                if(svsLocation.toString().contains(photo) && svsLocation.toString().equals(name)) {
                    return svsLocation;
                }
            }

            return null;
        }

        public int getIndex(){
            return index;
        }

        public static ArrayList<SVS_LOCATION> getArrayList(){   // 추후 pipe, pump 구분 요청, 대비

            ArrayList<SVS_LOCATION> svsLocations = new ArrayList<>();
            for(SVS_LOCATION svsLocation : SVS_LOCATION.values()){
                if("svs4".equals(svsLocation.title) )
                    continue;

//                svsLocation.title = "Vertical".equals(svsLocation.title) ? "svs1" : svsLocation.title;
//                svsLocation.title = "Horizontal".equals(svsLocation.title) ? "svs2" : svsLocation.title;
//                svsLocation.title = "Axial".equals(svsLocation.title) ? "svs3" : svsLocation.title;
                svsLocation.title = "svs1".equals(svsLocation.title) ? "Vertical" : svsLocation.title;
                svsLocation.title = "svs2".equals(svsLocation.title) ? "Horizontal" : svsLocation.title;
                svsLocation.title = "svs3".equals(svsLocation.title) ? "Axial" : svsLocation.title;

                svsLocations.add(svsLocation);
            }

            return svsLocations;
        }

        public static ArrayList<SVS_LOCATION> getArrayListPipe(){   // added by hslee 2020-06-16

            ArrayList<SVS_LOCATION> svsLocations = new ArrayList<>();
            for(SVS_LOCATION svsLocation : SVS_LOCATION.values()){
                if("svs4".equals(svsLocation.title) )
                    continue;

                svsLocation.title = "svs1".equals(svsLocation.title) ? "Vertical" : svsLocation.title;
                svsLocation.title = "svs2".equals(svsLocation.title) ? "Horizontal" : svsLocation.title;
                svsLocation.title = "svs3".equals(svsLocation.title) ? "Axial" : svsLocation.title;

                svsLocations.add(svsLocation);
            }

            return svsLocations;
        }
    }
}
