package kr.co.signallink.svsv2.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefComboBox;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.dto.HelloData;
import kr.co.signallink.svsv2.dto.SVSCode;
import kr.co.signallink.svsv2.dto.SVSFreq;
import kr.co.signallink.svsv2.dto.SVSParam;
import kr.co.signallink.svsv2.dto.SVSTime;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.ClipboardUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;

/**
 * Created by nspil on 2018-02-13.
 */

public class DiagnosisAdapter extends ArrayAdapter {

    private DefConstant.DIAGNOSIS_CATEGORY[] diagnosisCategories;

    @Override
    public int getViewTypeCount() {
        return DefConstant.DIAGNOSIS_CATEGORY.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        return diagnosisCategories[position].ordinal();
    }

    public DiagnosisAdapter(Context context, int resource, DefConstant.DIAGNOSIS_CATEGORY[] diagnosisCategories) {
        super(context, resource, diagnosisCategories);
        this.diagnosisCategories = diagnosisCategories;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        DefConstant.DIAGNOSIS_CATEGORY diagnosisCategory = diagnosisCategories[position];

        SVSParam svsParam = SVS.getInstance().getUploaddata().getSvsParam();
        SVSCode svsCode = svsParam.getCode();

        if (convertView == null)
        {
            if(diagnosisCategory == DefConstant.DIAGNOSIS_CATEGORY.SVS_SERIAL)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.diagnosislist_device_element, null);

                HelloData helloData = SVS.getInstance().getHellodata();
                if(helloData != null)
                {
                    final String serialNumber = helloData.getuSerialNo();
                    if(serialNumber != null)
                    {
                        TextView device_serial = convertView.findViewById(R.id.diagnosis_content_device_serial);
                        device_serial.setText(serialNumber);

                        device_serial.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                ClipboardUtil.copy(serialNumber);
                                ToastUtil.showShort("Serial Number copied to clipboard.");
                            }
                        });
                    }

                    TextView device_firmware = convertView.findViewById(R.id.diagnosis_content_device_firmware);
                    device_firmware.setText("v."+helloData.getuFwVer());

                    TextView device_hardware = convertView.findViewById(R.id.diagnosis_content_device_hardware);
                    device_hardware.setText("v."+helloData.getuHwVer());
                }
            }
            else if (diagnosisCategory == DefConstant.DIAGNOSIS_CATEGORY.OVERRALLSETTING)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.diagnosislist_overallsetting_element, null);

                TextView os_it = convertView.findViewById(R.id.diagnosis_content_os_itV);
                os_it.setText(String.valueOf(svsParam.getnIntervalTime()));

                TextView os_mr = convertView.findViewById(R.id.diagnosis_content_os_mrV);
                os_mr.setText(DefComboBox.measurerange(svsParam.getnMesRange()));

                TextView os_ma = convertView.findViewById(R.id.diagnosis_content_os_maV);
                os_ma.setText(DefComboBox.measureaxis(svsParam.getnMesAxis()));

                TextView os_sp = convertView.findViewById(R.id.diagnosis_content_os_spV);
                os_sp.setText(DefComboBox.samplingfreq(svsParam.getnSplFreq()));

                TextView os_or = convertView.findViewById(R.id.diagnosis_content_os_orV);
                os_or.setText(DefComboBox.offsetremoval(svsParam.getnOfsRemoval()));

                TextView os_oa = convertView.findViewById(R.id.diagnosis_content_os_oaV);
                os_oa.setText(String.valueOf(svsParam.getfOfsAdjust()));

                TextView os_dc = convertView.findViewById(R.id.diagnosis_content_os_dcV);
                os_dc.setText(DefComboBox.dataconversion(svsParam.getnDataConv()));

                TextView os_fac = convertView.findViewById(R.id.diagnosis_content_os_facV);
                os_fac.setText(String.valueOf(svsParam.getnFftAvg()));

                TextView os_sv = convertView.findViewById(R.id.diagnosis_content_os_svV);
                os_sv.setText(String.valueOf(svsParam.getfSensitivity()));
            }
            else if (diagnosisCategory == DefConstant.DIAGNOSIS_CATEGORY.LEARNINGPARAMETER)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.diagnosislist_learningparameter_element, null);

                TextView lp_lc = convertView.findViewById(R.id.diagnosis_content_lp_lcV);
                lp_lc.setText(String.valueOf(svsParam.getlLearnCnt()));

                TextView lp_of = convertView.findViewById(R.id.diagnosis_content_lp_ofV);
                lp_of.setText(String.valueOf(svsParam.getfLearnOffset()));

                TextView lp_sf = convertView.findViewById(R.id.diagnosis_content_lp_sfV);
                lp_sf.setText(String.valueOf(svsParam.getlLearnDev()));

                TextView lp_fco = convertView.findViewById(R.id.diagnosis_content_lp_fcoV);
                lp_fco.setText(String.valueOf(svsParam.getfFftCurveOffset()));

                TextView lp_lr = convertView.findViewById(R.id.diagnosis_content_lp_lrV);
                lp_lr.setText(String.valueOf(svsParam.getlLimitResolution()));
            }
            else if(diagnosisCategory == DefConstant.DIAGNOSIS_CATEGORY.DIAGNOSISDECISIONPARAMETERS)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.diagnosislist_diagnosisdecisionparameters_element, null);

                TextView ddp_dl = convertView.findViewById(R.id.diagnosis_content_ddp_dlV);
                ddp_dl.setText(String.valueOf(svsParam.getlDanLimit()));

                TextView ddp_wc = convertView.findViewById(R.id.diagnosis_content_ddp_wcV);
                ddp_wc.setText(String.valueOf(svsParam.getlWrnCnt()));

                TextView ddp_dc = convertView.findViewById(R.id.diagnosis_content_ddp_dcV);
                ddp_dc.setText(String.valueOf(svsParam.getlDanCnt()));
            }
            else if(diagnosisCategory == DefConstant.DIAGNOSIS_CATEGORY.MODE_DIAGNOSIS)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.diagnosislist_mode_element, null);

                TextView md_md = convertView.findViewById(R.id.diagnosis_content_md_mdV);
                md_md.setText(DefComboBox.mode((int)svsParam.getlPSaveCon()));

                TextView md_tc = convertView.findViewById(R.id.diagnosis_content_md_tcV);
                md_tc.setText(DefComboBox.trigercode((int)svsParam.getlPSaveValue()));

                TextView md_tv = convertView.findViewById(R.id.diagnosis_content_md_tvV);
                md_tv.setText(String.valueOf(svsParam.getfPSaveLevel()));
            }
            else if(diagnosisCategory == DefConstant.DIAGNOSIS_CATEGORY.DATALOGGING)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.diagnosislist_datalogging_element, null);

                CheckBox dl_ws = convertView.findViewById(R.id.diagnosis_content_dl_wsV);
                dl_ws.setChecked(svsParam.getlWrnLog() > 0);

                CheckBox dl_ds = convertView.findViewById(R.id.diagnosis_content_dl_dsV);
                dl_ds.setChecked(svsParam.getlDanLog() > 0);
            }
            else if(diagnosisCategory == DefConstant.DIAGNOSIS_CATEGORY.TEMPERATURE_DIAGNOSIS)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.diagnosislist_temperature_element, null);

                CheckBox tem_en = convertView.findViewById(R.id.diagnosis_content_tem_enV);
                tem_en.setChecked(svsCode.getlTempEna() > 0);

                TextView tem_lw = convertView.findViewById(R.id.diagnosis_content_tem_lwV);
                tem_lw.setText(String.valueOf(svsCode.getlTempWrn()));

                TextView tem_ld = convertView.findViewById(R.id.diagnosis_content_tem_ldV);
                tem_ld.setText(String.valueOf(svsCode.getlTempDan()));
            }
            else if(diagnosisCategory == DefConstant.DIAGNOSIS_CATEGORY.TIMECODES_DIAGNOSIS)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.diagnosislist_timecodes_element, null);

                SVSTime svsTime = svsCode.getTimeEna();

                CheckBox tc_en = convertView.findViewById(R.id.diagnosis_content_tc_en_rmsV);
                tc_en.setChecked(svsTime.getdRms() > 0);

                tc_en = convertView.findViewById(R.id.diagnosis_content_tc_en_pekV);
                tc_en.setChecked(svsTime.getdPeak() > 0);

                tc_en = convertView.findViewById(R.id.diagnosis_content_tc_en_crfV);
                tc_en.setChecked(svsTime.getdCrf() > 0);

                svsTime = svsCode.getTimeWrn();

                TextView tc_lw = convertView.findViewById(R.id.diagnosis_content_tc_lw_rmsV);
                tc_lw.setText(String.valueOf(svsTime.getdRms()));

                tc_lw = convertView.findViewById(R.id.diagnosis_content_tc_lw_pekV);
                tc_lw.setText(String.valueOf(svsTime.getdPeak()));

                tc_lw = convertView.findViewById(R.id.diagnosis_content_tc_lw_crfV);
                tc_lw.setText(String.valueOf(svsTime.getdCrf()));

                svsTime = svsCode.getTimeDan();

                TextView tc_ld = convertView.findViewById(R.id.diagnosis_content_tc_ld_rmsV);
                tc_ld.setText(String.valueOf(svsTime.getdRms()));

                tc_ld = convertView.findViewById(R.id.diagnosis_content_tc_ld_pekV);
                tc_ld.setText(String.valueOf(svsTime.getdPeak()));

                tc_ld = convertView.findViewById(R.id.diagnosis_content_tc_ld_crfV);
                tc_ld.setText(String.valueOf(svsTime.getdCrf()));
            }
            else if(diagnosisCategory == DefConstant.DIAGNOSIS_CATEGORY.FREQUENCYPEAKCODES_DIAGNOSIS)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.diagnosislist_frequencypeakcodes_element, null);

                SVSFreq[] svsFreq = svsCode.getFreqEna();

                CheckBox tc_en = convertView.findViewById(R.id.diagnosis_content_fpc_en1V);
                tc_en.setChecked(svsFreq[0].getdPeak() > 0);

                tc_en = convertView.findViewById(R.id.diagnosis_content_fpc_en2V);
                tc_en.setChecked(svsFreq[1].getdPeak() > 0);

                tc_en = convertView.findViewById(R.id.diagnosis_content_fpc_en3V);
                tc_en.setChecked(svsFreq[2].getdPeak() > 0);

                tc_en = convertView.findViewById(R.id.diagnosis_content_fpc_en4V);
                tc_en.setChecked(svsFreq[3].getdPeak() > 0);

                tc_en = convertView.findViewById(R.id.diagnosis_content_fpc_en5V);
                tc_en.setChecked(svsFreq[4].getdPeak() > 0);

                tc_en = convertView.findViewById(R.id.diagnosis_content_fpc_en6V);
                tc_en.setChecked(svsFreq[5].getdPeak() > 0);

                svsFreq = svsCode.getFreqMin();

                TextView fpc_mi = convertView.findViewById(R.id.diagnosis_content_fpc_mi1V);
                fpc_mi.setText(String.valueOf(svsFreq[0].getdPeak()));

                fpc_mi = convertView.findViewById(R.id.diagnosis_content_fpc_mi2V);
                fpc_mi.setText(String.valueOf(svsFreq[1].getdPeak()));

                fpc_mi = convertView.findViewById(R.id.diagnosis_content_fpc_mi3V);
                fpc_mi.setText(String.valueOf(svsFreq[2].getdPeak()));

                fpc_mi = convertView.findViewById(R.id.diagnosis_content_fpc_mi4V);
                fpc_mi.setText(String.valueOf(svsFreq[3].getdPeak()));

                fpc_mi = convertView.findViewById(R.id.diagnosis_content_fpc_mi5V);
                fpc_mi.setText(String.valueOf(svsFreq[4].getdPeak()));

                fpc_mi = convertView.findViewById(R.id.diagnosis_content_fpc_mi6V);
                fpc_mi.setText(String.valueOf(svsFreq[5].getdPeak()));

                svsFreq = svsCode.getFreqMax();

                TextView fpc_ma = convertView.findViewById(R.id.diagnosis_content_fpc_ma1V);
                fpc_ma.setText(String.valueOf(svsFreq[0].getdPeak()));

                fpc_ma = convertView.findViewById(R.id.diagnosis_content_fpc_ma2V);
                fpc_ma.setText(String.valueOf(svsFreq[1].getdPeak()));

                fpc_ma = convertView.findViewById(R.id.diagnosis_content_fpc_ma3V);
                fpc_ma.setText(String.valueOf(svsFreq[2].getdPeak()));

                fpc_ma = convertView.findViewById(R.id.diagnosis_content_fpc_ma4V);
                fpc_ma.setText(String.valueOf(svsFreq[3].getdPeak()));

                fpc_ma = convertView.findViewById(R.id.diagnosis_content_fpc_ma5V);
                fpc_ma.setText(String.valueOf(svsFreq[4].getdPeak()));

                fpc_ma = convertView.findViewById(R.id.diagnosis_content_fpc_ma6V);
                fpc_ma.setText(String.valueOf(svsFreq[5].getdPeak()));

                svsFreq = svsCode.getFreqWrn();

                TextView fpc_lw = convertView.findViewById(R.id.diagnosis_content_fpc_lw1V);
                fpc_lw.setText(String.valueOf(svsFreq[0].getdPeak()));

                fpc_lw = convertView.findViewById(R.id.diagnosis_content_fpc_lw2V);
                fpc_lw.setText(String.valueOf(svsFreq[1].getdPeak()));

                fpc_lw = convertView.findViewById(R.id.diagnosis_content_fpc_lw3V);
                fpc_lw.setText(String.valueOf(svsFreq[2].getdPeak()));

                fpc_lw = convertView.findViewById(R.id.diagnosis_content_fpc_lw4V);
                fpc_lw.setText(String.valueOf(svsFreq[3].getdPeak()));

                fpc_lw = convertView.findViewById(R.id.diagnosis_content_fpc_lw5V);
                fpc_lw.setText(String.valueOf(svsFreq[4].getdPeak()));

                fpc_lw = convertView.findViewById(R.id.diagnosis_content_fpc_lw6V);
                fpc_lw.setText(String.valueOf(svsFreq[5].getdPeak()));

                svsFreq = svsCode.getFreqDan();

                TextView fpc_ld = convertView.findViewById(R.id.diagnosis_content_fpc_ld1V);
                fpc_ld.setText(String.valueOf(svsFreq[0].getdPeak()));

                fpc_ld = convertView.findViewById(R.id.diagnosis_content_fpc_ld2V);
                fpc_ld.setText(String.valueOf(svsFreq[1].getdPeak()));

                fpc_ld = convertView.findViewById(R.id.diagnosis_content_fpc_ld3V);
                fpc_ld.setText(String.valueOf(svsFreq[2].getdPeak()));

                fpc_ld = convertView.findViewById(R.id.diagnosis_content_fpc_ld4V);
                fpc_ld.setText(String.valueOf(svsFreq[3].getdPeak()));

                fpc_ld = convertView.findViewById(R.id.diagnosis_content_fpc_ld5V);
                fpc_ld.setText(String.valueOf(svsFreq[4].getdPeak()));

                fpc_ld = convertView.findViewById(R.id.diagnosis_content_fpc_ld6V);
                fpc_ld.setText(String.valueOf(svsFreq[5].getdPeak()));
            }
            else if(diagnosisCategory == DefConstant.DIAGNOSIS_CATEGORY.FREQUENCYBANDCODES_DIAGNOSIS)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.diagnosislist_frequencybandcodes_element, null);

                SVSFreq[] svsFreq = svsCode.getFreqEna();

                CheckBox fbc_en = convertView.findViewById(R.id.diagnosis_content_fbc_en1V);
                fbc_en.setChecked(svsFreq[0].getdBnd() > 0);

                fbc_en = convertView.findViewById(R.id.diagnosis_content_fbc_en2V);
                fbc_en.setChecked(svsFreq[1].getdBnd() > 0);

                fbc_en = convertView.findViewById(R.id.diagnosis_content_fbc_en3V);
                fbc_en.setChecked(svsFreq[2].getdBnd() > 0);

                fbc_en = convertView.findViewById(R.id.diagnosis_content_fbc_en4V);
                fbc_en.setChecked(svsFreq[3].getdBnd() > 0);

                fbc_en = convertView.findViewById(R.id.diagnosis_content_fbc_en5V);
                fbc_en.setChecked(svsFreq[4].getdBnd() > 0);

                fbc_en = convertView.findViewById(R.id.diagnosis_content_fbc_en6V);
                fbc_en.setChecked(svsFreq[5].getdBnd() > 0);

                svsFreq = svsCode.getFreqMin();

                TextView fbc_mi = convertView.findViewById(R.id.diagnosis_content_fbc_mi1V);
                fbc_mi.setText(String.valueOf(svsFreq[0].getdBnd()));

                fbc_mi = convertView.findViewById(R.id.diagnosis_content_fbc_mi2V);
                fbc_mi.setText(String.valueOf(svsFreq[1].getdBnd()));

                fbc_mi = convertView.findViewById(R.id.diagnosis_content_fbc_mi3V);
                fbc_mi.setText(String.valueOf(svsFreq[2].getdBnd()));

                fbc_mi = convertView.findViewById(R.id.diagnosis_content_fbc_mi4V);
                fbc_mi.setText(String.valueOf(svsFreq[3].getdBnd()));

                fbc_mi = convertView.findViewById(R.id.diagnosis_content_fbc_mi5V);
                fbc_mi.setText(String.valueOf(svsFreq[4].getdBnd()));

                fbc_mi = convertView.findViewById(R.id.diagnosis_content_fbc_mi6V);
                fbc_mi.setText(String.valueOf(svsFreq[5].getdBnd()));

                svsFreq = svsCode.getFreqMax();

                TextView fbc_ma = convertView.findViewById(R.id.diagnosis_content_fbc_ma1V);
                fbc_ma.setText(String.valueOf(svsFreq[0].getdBnd()));

                fbc_ma = convertView.findViewById(R.id.diagnosis_content_fbc_ma2V);
                fbc_ma.setText(String.valueOf(svsFreq[1].getdBnd()));

                fbc_ma = convertView.findViewById(R.id.diagnosis_content_fbc_ma3V);
                fbc_ma.setText(String.valueOf(svsFreq[2].getdBnd()));

                fbc_ma = convertView.findViewById(R.id.diagnosis_content_fbc_ma4V);
                fbc_ma.setText(String.valueOf(svsFreq[3].getdBnd()));

                fbc_ma = convertView.findViewById(R.id.diagnosis_content_fbc_ma5V);
                fbc_ma.setText(String.valueOf(svsFreq[4].getdBnd()));

                fbc_ma = convertView.findViewById(R.id.diagnosis_content_fbc_ma6V);
                fbc_ma.setText(String.valueOf(svsFreq[5].getdBnd()));

                svsFreq = svsCode.getFreqWrn();

                TextView fbc_lw = convertView.findViewById(R.id.diagnosis_content_fbc_lw1V);
                fbc_lw.setText(String.valueOf(svsFreq[0].getdBnd()));

                fbc_lw = convertView.findViewById(R.id.diagnosis_content_fbc_lw2V);
                fbc_lw.setText(String.valueOf(svsFreq[1].getdBnd()));

                fbc_lw = convertView.findViewById(R.id.diagnosis_content_fbc_lw3V);
                fbc_lw.setText(String.valueOf(svsFreq[2].getdBnd()));

                fbc_lw = convertView.findViewById(R.id.diagnosis_content_fbc_lw4V);
                fbc_lw.setText(String.valueOf(svsFreq[3].getdBnd()));

                fbc_lw = convertView.findViewById(R.id.diagnosis_content_fbc_lw5V);
                fbc_lw.setText(String.valueOf(svsFreq[4].getdBnd()));

                fbc_lw = convertView.findViewById(R.id.diagnosis_content_fbc_lw6V);
                fbc_lw.setText(String.valueOf(svsFreq[5].getdBnd()));

                svsFreq = svsCode.getFreqDan();

                TextView fbc_ld = convertView.findViewById(R.id.diagnosis_content_fbc_ld1V);
                fbc_ld.setText(String.valueOf(svsFreq[0].getdBnd()));

                fbc_ld = convertView.findViewById(R.id.diagnosis_content_fbc_ld2V);
                fbc_ld.setText(String.valueOf(svsFreq[1].getdBnd()));

                fbc_ld = convertView.findViewById(R.id.diagnosis_content_fbc_ld3V);
                fbc_ld.setText(String.valueOf(svsFreq[2].getdBnd()));

                fbc_ld = convertView.findViewById(R.id.diagnosis_content_fbc_ld4V);
                fbc_ld.setText(String.valueOf(svsFreq[3].getdBnd()));

                fbc_ld = convertView.findViewById(R.id.diagnosis_content_fbc_ld5V);
                fbc_ld.setText(String.valueOf(svsFreq[4].getdBnd()));

                fbc_ld = convertView.findViewById(R.id.diagnosis_content_fbc_ld6V);
                fbc_ld.setText(String.valueOf(svsFreq[5].getdBnd()));
            }
            else if(diagnosisCategory == DefConstant.DIAGNOSIS_CATEGORY.BEEP_AND_OR_CONDITION)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.diagnosislist_beep_and_flag_element, null);

                CheckBox andflag_en =  convertView.findViewById(R.id.diagnosis_content_andflag_enV);
                andflag_en.setChecked(svsParam.getnAndFlag() > 0);
            }
            else if(diagnosisCategory == DefConstant.DIAGNOSIS_CATEGORY.FFTCURVELIMIT_DIAGNOSIS)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.diagnosislist_fftcurvelimit_element, null);

                CheckBox fcl_en =  convertView.findViewById(R.id.diagnosis_content_fcl_enV);
                fcl_en.setChecked(svsParam.getlFcEna() > 0);

                TextView fcl_wr =  convertView.findViewById(R.id.diagnosis_content_fcl_wrV);
                fcl_wr.setText(String.valueOf(svsParam.getlFcWrn()));

                TextView fcl_da =  convertView.findViewById(R.id.diagnosis_content_fcl_daV);
                fcl_da.setText(String.valueOf(svsParam.getlFcDan()));
            }
        }
        return convertView;
    }
}
