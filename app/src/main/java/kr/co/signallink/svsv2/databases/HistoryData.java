package kr.co.signallink.svsv2.databases;

import java.util.ArrayList;

import kr.co.signallink.svsv2.commons.DefCMDOffset;
import kr.co.signallink.svsv2.dto.MeasureData;
import kr.co.signallink.svsv2.dto.SVSFreq;
import kr.co.signallink.svsv2.dto.SVSTime;
import kr.co.signallink.svsv2.dto.UploadData;

public class HistoryData {

    private MeasureData averagemeasure = new MeasureData();
    private UploadData uploaddata = new UploadData();

    public UploadData getUploaddata() {
        return uploaddata;
    }

    public void setUploaddata(UploadData uploaddata) {
        this.uploaddata = uploaddata;
    }

    public MeasureData getAveragemeasure() {
        return averagemeasure;
    }

    public void setAveragemeasure(MeasureData averagemeasure) {
        this.averagemeasure = averagemeasure;
    }

    public void calcAverageMeasure(ArrayList<MeasureData> measureDatas) {
        final int measureDataCount = measureDatas.size();
        MeasureData averageMeasure = new MeasureData();

        if(measureDatas.size() > 0) {
            averageMeasure = measureDatas.get(0);

            SVSTime averagetimecur = averageMeasure.getSvsTime();
            SVSFreq[] averagefreqcur = averageMeasure.getSvsFreq();

            //데이터를 한곳에 누적해서 쌓기 (평균값을 구하기전에 다 더하기)
            for(int i=1; i<measureDataCount; i++) {
                SVSTime timecur = measureDatas.get(i).getSvsTime();
                averagetimecur.setdPeak(averagetimecur.getdPeak() + timecur.getdPeak());
                averagetimecur.setdRms(averagetimecur.getdRms() + timecur.getdRms());
                averagetimecur.setdCrf(averagetimecur.getdCrf() + timecur.getdCrf());

                SVSFreq[] freqcur = measureDatas.get(i).getSvsFreq();
                for(int j = 0; j<DefCMDOffset.BAND_MAX; j++) {
                    averagefreqcur[j].setdPeak(averagefreqcur[j].getdPeak() + freqcur[j].getdPeak());
                    averagefreqcur[j].setdBnd(averagefreqcur[j].getdBnd() + freqcur[j].getdBnd());
                }
            }

            //갯수로 평균하기
            averagetimecur.setdPeak(averagetimecur.getdPeak() / (float)measureDataCount);
            averagetimecur.setdRms(averagetimecur.getdRms() / (float)measureDataCount);
            averagetimecur.setdCrf(averagetimecur.getdCrf() / (float)measureDataCount);

            for(int i = 0; i<DefCMDOffset.BAND_MAX; i++) {
                averagefreqcur[i].setdPeak(averagefreqcur[i].getdPeak() / (float)measureDataCount);
                averagefreqcur[i].setdBnd(averagefreqcur[i].getdBnd() / (float)measureDataCount);
            }
        }

        this.averagemeasure = averageMeasure;
    }
}
