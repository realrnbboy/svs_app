package kr.co.signallink.svsv2.databases;

import java.util.ArrayList;

import kr.co.signallink.svsv2.commons.DefCMDOffset;
import kr.co.signallink.svsv2.dto.MeasureData;
import kr.co.signallink.svsv2.dto.SVSFreq;
import kr.co.signallink.svsv2.dto.SVSTime;
import kr.co.signallink.svsv2.dto.UploadData;

public class HistoryEntity {

    private UploadData uploaddata = new UploadData();
    private MeasureData averagemeasure = new MeasureData();

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

    public void calcAverageMeasure(ArrayList<MeasureData> measuredatas) {
        MeasureData averagemeasure = new MeasureData();

        int measureDatasSize = measuredatas.size();
        if(measureDatasSize > 0) {
            averagemeasure = measuredatas.get(0);

            SVSTime averagetimecur = averagemeasure.getSvsTime();
            SVSFreq[] averagefreqcur = averagemeasure.getSvsFreq();

            for(int i=1; i<measureDatasSize; i++) {
                SVSTime timecur = measuredatas.get(i).getSvsTime();
                averagetimecur.setdPeak(averagetimecur.getdPeak() + timecur.getdPeak());
                averagetimecur.setdRms(averagetimecur.getdRms() + timecur.getdRms());
                averagetimecur.setdCrf(averagetimecur.getdCrf() + timecur.getdCrf());

                SVSFreq[] freqcur = measuredatas.get(i).getSvsFreq();
                for(int j = 0; j< DefCMDOffset.BAND_MAX; j++) {
                    averagefreqcur[j].setdPeak(averagefreqcur[j].getdPeak() + freqcur[j].getdPeak());
                    averagefreqcur[j].setdBnd(averagefreqcur[j].getdBnd() + freqcur[j].getdBnd());
                }
            }

            averagetimecur.setdPeak(averagetimecur.getdPeak() / (float)measureDatasSize);
            averagetimecur.setdRms(averagetimecur.getdRms() / (float)measureDatasSize);
            averagetimecur.setdCrf(averagetimecur.getdCrf() / (float)measureDatasSize);

            for(int i = 0; i<DefCMDOffset.BAND_MAX; i++) {
                averagefreqcur[i].setdPeak(averagefreqcur[i].getdPeak() / (float)measureDatasSize);
                averagefreqcur[i].setdBnd(averagefreqcur[i].getdBnd() / (float)measureDatasSize);
            }
        }

        this.averagemeasure = averagemeasure;
    }
}
