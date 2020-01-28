package kr.co.signallink.svsv2.commons;

import java.util.Arrays;
import java.util.List;

public class DefPhoto {

    public enum PICK {

        NONE(-1),

        ALBUM_0(5),
        ALBUM_1(6),
        ALBUM_2(7),
        ALBUM_3(8),
        ALBUM_4(9),

        CAMERA_0(10),
        CAMERA_1(11),
        CAMERA_2(12),
        CAMERA_3(13),
        CAMERA_4(14);

        private int globalIndex;
        private PICK(int globalIndex){
            this.globalIndex = globalIndex;
        }

        public int getGlobalIndex(){
            return globalIndex;
        }
        public int getLocalIndex(){

            if(ALBUM_0.globalIndex <= this.globalIndex && this.globalIndex <= ALBUM_4.globalIndex){
                return globalIndex - ALBUM_0.getGlobalIndex();
            }
            else if(CAMERA_0.globalIndex <= this.globalIndex && this.globalIndex <= CAMERA_4.globalIndex)
            {
                return globalIndex - CAMERA_0.getGlobalIndex();
            }

            return NONE.globalIndex;
        }

        @Override
        public String toString() {
            return ""+ getLocalIndex();
        }
    }

    public static DefPhoto.PICK findTarget(int globalIndex){
        for(PICK pick : PICK.values()){
            if(pick.getGlobalIndex() == globalIndex){
                return pick;
            }
        }

        return PICK.NONE;
    }

    public enum PICKS {

        CAMERA(Arrays.asList(PICK.CAMERA_0, PICK.CAMERA_1, PICK.CAMERA_2, PICK.CAMERA_3, PICK.CAMERA_4)),
        ALBUM(Arrays.asList(PICK.ALBUM_0, PICK.ALBUM_1, PICK.ALBUM_2, PICK.ALBUM_3, PICK.ALBUM_4));

        private List<PICK> list;
        private PICKS(List<PICK> list){
            this.list = list;
        }

        public boolean hasTarget(PICK target){
            for(PICK pick : list){
                if(pick == target){
                    return true;
                }
            }

            return false;
        }

        public PICK getPICK(int localIndex){
            for(PICK pick : list){
                if(pick.getLocalIndex() == localIndex){
                    return pick;
                }
            }

            return PICK.NONE;
        }

        public int size(){
            return list.size();
        }

    }

}
