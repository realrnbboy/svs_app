package kr.co.signallink.svsv2.sort;

import java.util.Comparator;

import kr.co.signallink.svsv2.user.RegisterSVSData;

public class SortManager {

    public enum SORT_TYPE {
        NAME_ADDRESS("Name | Address", SortManager.sortByName),
        RSSI_NAME("RSSI", SortManager.sortByRSSI);

        private String title;
        private Comparator comparator;
        private SORT_TYPE(String title, Comparator comparator){
            this.title = title;
            this.comparator = comparator;
        }

        @Override
        public String toString() {
            return title;
        }

        public Comparator getComparator() {
            return comparator;
        }
    }

    public static Comparator<RegisterSVSData> sortByName = new Comparator<RegisterSVSData>() {

        @Override
        public int compare(RegisterSVSData o1, RegisterSVSData o2) {

            //이름 순 정렬
            //이름이 같으면, Address 순 정렬

            if(!o1.getName().equals(o2.getName()))
            {
                return o1.getName().compareTo(o2.getName());
            }
            else
            {
                return o1.getAddress().compareTo(o2.getAddress());
            }
        }
    };

    public static Comparator<RegisterSVSData> sortByRSSI = new Comparator<RegisterSVSData>() {

        @Override
        public int compare(RegisterSVSData o1, RegisterSVSData o2) {

            //연결된 데이터를 우선으로 보여줌
            if(o1.isLinked() && !o2.isLinked())
            {
                return 1;
            }

            //RSSI 순 정렬
            //RSSI가 같으면, 이름 순 정렬
            //이름이 같으면, Address 순 정렬
            if(o1.getRssi() != o2.getRssi())
            {
                return o2.getRssi()-o1.getRssi();
            }
            else if(!o1.getName().equals(o2.getName()))
            {
                return o1.getName().compareTo(o2.getName());
            }
            else
            {
                return o1.getAddress().compareTo(o2.getAddress());
            }
        }
    };


}
