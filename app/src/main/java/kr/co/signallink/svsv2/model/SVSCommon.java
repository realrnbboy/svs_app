package kr.co.signallink.svsv2.model;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class SVSCommon {
    public String fnConvertScale2String(int nScale) {
        String strScale = "";
        switch (nScale) {
        case 0:
            strScale = "2g";
            break;
        case 1:
            strScale = "4g";
            break;
        case 2:
            strScale = "8g";
            break;
        case 3:
            strScale = "16g";
            break;
        default:
            break;
        }

        return strScale;
    }

    public String Byte2String(byte[] data) {
        // String str = Encoding.Default.GetString(data);
        if (data == null) {
            return null;
        }
        String str = data.toString();

        return str;
    }

    public byte[] String2Byte(String str) {
        if (str == null) {
            return null;
        }
        // byte[] result = Encoding.UTF8.GetBytes(str);
        byte[] result = null;
        try {
            result = str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }

    /// <summary>
    /// data 내 갑들의 제곱합에 대한 제곱근 값 반환
    /// bRMS가 TRUE이면, SQRT(SUMSQ(..) / 1.5)
    /// bRMS가 FALSE이면, SQRT(SUMSQ(..))
    /// </summary>
    /// <param name="data"></param>
    /// <param name="bRMS"></param>
    /// <returns></returns>
    public double fnSQRT_SUMSQ(double[] data, boolean bRMS)
    {
        double dResult = 0.0;
        double dSumSQ = 0;

        try
        {
            for (int i = 0; i < data.length; i++)
                dSumSQ += Math.pow(data[i], 2);

            if (bRMS)
                dResult = Math.sqrt(dSumSQ / 1.5);
            else
                dResult = Math.sqrt(dSumSQ);
        }
        catch (Exception ex)
        {
            System.out.println("[Exception: fnSQRT_SUMSQ] " + ex.getMessage());
            //Console.WriteLine("[Exception: fnSQRT_SUMSQ] " + ex.Message);
        }

        return dResult;
    }

    /// <summary>
    /// 평균값 추출
    /// </summary>
    /// <param name="data"></param>
    /// <returns></returns>
    public double fnMEAN(double[] data)
    {
        double dResult = 0.0;
        double dSum = 0;

        try
        {
            for (int i = 0; i < data.length; i++)
                dSum += data[i];

            dResult = dSum / data.length;
        }
        catch (Exception ex)
        {
            System.out.println("[Exception: fnMEAN] " + ex.getMessage());
            //Console.WriteLine("[Exception: fnMEAN] " + ex.Message);
        }

        return dResult;
    }

    /// <summary>
    /// 백분위수 계산 함수
    /// </summary>
    /// <param name="sequence"></param>
    /// <param name="fPercentile"></param>
    /// <returns></returns>
    public double fnPercentile(double[] sequence, double fPercentile)
    {
        Arrays.sort(sequence);
        int N = sequence.length;
        double n = (N - 1) * fPercentile + 1;
        // Another method: double n = (N + 1) * excelPercentile;
        if (n == 1d)
            return sequence[0];
        else if (n == N)
            return sequence[N - 1];
        else
        {
            int k = (int)n;
            double d = n - k;
            return sequence[k - 1] + d * (sequence[k] - sequence[k - 1]);
        }
    }

    /// <summary>
    /// 입력 배열의 중간값 반환
    /// </summary>
    /// <param name="data1"></param>
    /// <param name="data2"></param>
    /// <param name="data3"></param>
    /// <returns></returns>
    public double fnMedian(double data1, double data2, double data3)
    {
        double median = 0;
        double[] tmp = {data1, data2, data3};

        Arrays.sort(tmp);
        median = tmp[1];

        return median;
    }

    public static double max(double[] t) {
        if( t == null ) {
            return 0;
        }
        
        double maximum = t[0]; // start with the first value
        for (int i=1; i<t.length; i++) {
            if (t[i] > maximum) {
                maximum = t[i];   // new maximum
            }
        }

        return maximum;
    }
}