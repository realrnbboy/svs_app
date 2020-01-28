package kr.co.signallink.svsv2.services;

import kr.co.signallink.svsv2.model.MainData;
import kr.co.signallink.svsv2.model.SVSCommon;
import kr.co.signallink.svsv2.model.VARIABLES_1_Type;
import kr.co.signallink.svsv2.model.VARIABLES_2_Type;

import java.util.Arrays;
import java.util.Collections;

import kr.co.signallink.svsv2.model.Constants;
import kr.co.signallink.svsv2.model.DIAGNOSIS_DATA_Type;
import kr.co.signallink.svsv2.model.MATRIX_2_Type;
import kr.co.signallink.svsv2.utils.Utils;

// using System;
// using System.Collections.Generic;
// using System.Linq;
// using System.Text;
// using System.Threading.Tasks;

// using System.Runtime.InteropServices;
// //using MathNet.Numerics.Statistics;

// namespace hdSVSM2
// {
/// <summary>
/// 진단 분석용  클래스
/// </summary>
public class DiagnosisInfo {
    // MainForm mainform;
    SVSCommon svsCommon;

    private float nFreq_Lower = 5;
    private float nFreq_Upper = 1000;
    public float[] aFeatureValues;
    public float[] aFeatureRanges;
    public float[] aFeatureLowers;
    public float[] aFeatureUppers;
    public double[] aFreqData1;
    public double[] aFreqData2;
    public double[] aFreqData3;

    public double[][] aVar2Data1; // 2차원 배열 : raw 데이터 수 x feature 수
    public double[][] aVar2Data2; // 2차원 배열 : raw 데이터 수 x feature 수
    public double[][] aVar2Data3; // 2차원 배열 : raw 데이터 수 x feature 수

    public VARIABLES_1_Type diagVar1;
    public VARIABLES_2_Type valueVar2, rangeVar2, lowerVar2, upperVar2;
    private double[][] tableFeature; // Mainform에서 초기 저장한 Feature 가중치 테이블
    private double[][] tableResult; // Matrix2 결과와 tableFeature 연산 결과 저장용 테이블
    private int nCauseCount = 0;

    MainData mainform;

    public DiagnosisInfo(MainData form) {
        mainform = form;
        svsCommon = new SVSCommon();

        aFeatureValues = new float[Constants.FEATURE_COUNT + 3]; // Constants.FEATURE_COUNT = 25, A>R, A<R, HORZ/VERT 추가
        aFeatureRanges = new float[Constants.FEATURE_COUNT + 3];
        aFeatureLowers = new float[Constants.FEATURE_COUNT + 3];
        aFeatureUppers = new float[Constants.FEATURE_COUNT + 3];

        aFreqData1 = new double[Constants.FREQ_ELE];
        aFreqData2 = new double[Constants.FREQ_ELE];
        aFreqData3 = new double[Constants.FREQ_ELE];

        aVar2Data1 = new double[Constants.FREQ_ELE][Constants.FEATURE_COUNT];
        aVar2Data2 = new double[Constants.FREQ_ELE][Constants.FEATURE_COUNT];
        aVar2Data3 = new double[Constants.FREQ_ELE][Constants.FEATURE_COUNT];

        diagVar1 = mainform.diagVar1;
        valueVar2 = mainform.valueVar2;
        rangeVar2 = mainform.rangeVar2;
        lowerVar2 = mainform.lowerVar2;
        upperVar2 = mainform.upperVar2;

        nCauseCount = mainform.featureInfos.nCount;
        tableFeature = new double[nCauseCount][Constants.FEATURE_COUNT];
        tableResult = new double[nCauseCount][Constants.FEATURE_COUNT];
        for (int row = 0; row < nCauseCount; row++) {
            for (int col = 0; col < Constants.FEATURE_COUNT; col++) // A>R, R>A, Hor/Ver/, 1x, 2x, ..., LF, Overall
                                                                    // RMS까지 --> 25개
            {
                tableFeature[row][col] = mainform.featureInfos.infos[row].fValues[col];
            }
        }
    }

    /// <summary>
    /// 설정된 Variable 2 값을 토대로 측정 데이터 분석용 임시 배열 초기화
    /// </summary>
    /// <param name="value"></param>
    /// <param name="range"></param>
    /// <param name="lower"></param>
    /// <param name="upper"></param>
    public void fnInitFeature(VARIABLES_2_Type value, VARIABLES_2_Type range, VARIABLES_2_Type lower,
            VARIABLES_2_Type upper) {
        try {
            // Value
            aFeatureValues[0] = 0; // Axial > Radial
            aFeatureValues[1] = 0; // Radial > Axial
            aFeatureRanges[0] = 0; // Axial > Radial
            aFeatureRanges[1] = 0; // Radial > Axial
            aFeatureLowers[0] = 0; // Axial > Radial
            aFeatureLowers[1] = 0; // Radial > Axial
            aFeatureUppers[0] = 0; // Axial > Radial
            aFeatureUppers[1] = 0; // Radial > Axial

            aFeatureValues[2] = 0; // Radial > Axial
            aFeatureRanges[2] = 0; // Radial > Axial
            aFeatureLowers[2] = 0; // Radial > Axial
            aFeatureUppers[2] = 0; // Radial > Axial

            System.arraycopy(value.data, 0, aFeatureValues, 3, value.data.length);
            System.arraycopy(range.data, 0, aFeatureRanges, 3, range.data.length);

            for (int i = 0; i < Constants.FEATURE_COUNT; i++) {
                if (i != 14 && i != 15) // NoiseFloor1, 2가 아닌 경우
                {
                    lower.data[i] = Utils.floatFloor((float) (value.data[i] * (1 - (range.data[i] * 0.01))));
                    upper.data[i] = Utils.floatFloor((float) (value.data[i] * (1 + (range.data[i] * 0.01))));
                    double t = (double) (value.data[i] * (1 + (range.data[i] * 0.01)));
                    t = 59.65 * (1 + (3 * 0.01));
                    t =t;
                } else if (i == 14) // NoiseFloor1인 경우
                {
                    lower.data[i] = 1;
                    upper.data[i] = value.data[i];
                } else if (i == 15) // NoiseFloor2인 경우
                {
                    lower.data[i] = value.data[i];
                    upper.data[i] = 1000;
                }
            }

            System.arraycopy(lower.data, 0, aFeatureLowers, 3, lower.data.length);
            System.arraycopy(upper.data, 0, aFeatureUppers, 3, upper.data.length);
        } catch (Exception ex) {
            System.out.println("[Exception: fnInitFeature] " + ex.getMessage());
            // Console.WriteLine("[Exception: fnInitFeature] " + ex.Message);
        }
    }

    /// <summary>
    /// 측정된 Raw 데이터로부터 MATRIX 2 추출
    /// </summary>
    /// <param name="measureData1"></param>
    /// <param name="measureData2"></param>
    /// <param name="measureData3"></param>
    /// <returns></returns>
    public MATRIX_2_Type fnMakeMatrix2(DIAGNOSIS_DATA_Type measureData1, DIAGNOSIS_DATA_Type measureData2,
            DIAGNOSIS_DATA_Type measureData3) {
        MATRIX_2_Type resultMatrix2 = new MATRIX_2_Type();
        resultMatrix2.aData1 = new double[Constants.FEATURE_COUNT + 3]; // Constants.FEATURE_COUNT = 25, A>R, A<R 추가
        resultMatrix2.aData2 = new double[Constants.FEATURE_COUNT + 3];
        resultMatrix2.aData3 = new double[Constants.FEATURE_COUNT + 3];
        resultMatrix2.aDataMax = new double[Constants.FEATURE_COUNT + 3];
        resultMatrix2.aDataMed = new double[Constants.FEATURE_COUNT + 3];
        resultMatrix2.aResult = new double[Constants.FEATURE_COUNT + 3];

        try {
            // raw 데이터별 index 생성 및 저장용
            // Arrays.fill(aFreqData1, 0);
            // Arrays.fill(aFreqData2, 0);
            // Arrays.fill(aFreqData3, 0);

            // Arrays.fill(aVar2Data1, 0.0);
            // Arrays.fill(aVar2Data2, 0.0);
            // Arrays.fill(aVar2Data3, 0.0);

            fnInitFeature(valueVar2, rangeVar2, lowerVar2, upperVar2);

            // System.arraycopy(measureData1.dFreq, aFreqData1, Constants.FREQ_ELE);
            // System.arraycopy(measureData2.dFreq, aFreqData2, Constants.FREQ_ELE);
            // System.arraycopy(measureData3.dFreq, aFreqData3, Constants.FREQ_ELE);

            for (int i = 0; i < Constants.FREQ_ELE; i++) {
                // i / 측정 데이터의 SplRate
                aFreqData1[i] = ((i + 1) < nFreq_Lower || (i + 1) > nFreq_Upper) ? 0
                        : (double) (i * (measureData1.fSamplingRate / 2) / Constants.FREQ_ELE);
                aFreqData2[i] = ((i + 1) < nFreq_Lower || (i + 1) > nFreq_Upper) ? 0
                        : (double) (i * (measureData2.fSamplingRate / 2) / Constants.FREQ_ELE);
                aFreqData3[i] = ((i + 1) < nFreq_Lower || (i + 1) > nFreq_Upper) ? 0
                        : (double) (i * (measureData3.fSamplingRate / 2) / Constants.FREQ_ELE);
                // aFreqData1[i] = ((i + 1) < nFreq_Lower || (i + 1) > nFreq_Upper) ? 0 :
                // aFreqData1[i];
                // aFreqData2[i] = ((i + 1) < nFreq_Lower || (i + 1) > nFreq_Upper) ? 0 :
                // aFreqData2[i];
                // aFreqData3[i] = ((i + 1) < nFreq_Lower || (i + 1) > nFreq_Upper) ? 0 :
                // aFreqData3[i];

                if (aFreqData1[i] == 0) {
                    measureData1.dPwrSpectrum[i] = 0;
                    measureData2.dPwrSpectrum[i] = 0;
                    measureData3.dPwrSpectrum[i] = 0;
                }
            }

            // 측정 raw 데이터별 Matrix 2 추출용 배열 생성(FREQ_ELE x Feature 수)
            for (int row = 0; row < Constants.FREQ_ELE; row++) {
                // 해당 DataIndex가 Variable2의 해당 Feature의 Lower보다 크고,
                // 해당 Feature의 Upper보다 작으면 측정 raw 데이터
                // 그렇지 않으면, 0
                // A>R, R>A는 절차에서 Skip
                for (int col = 0; col < Constants.FEATURE_COUNT; col++) {
                    if (row == 45)
                        row = row;
                    aVar2Data1[row][col] = (aFreqData1[row] > aFeatureLowers[col + 3]
                            && aFreqData1[row] < aFeatureUppers[col + 3]) ? measureData1.dPwrSpectrum[row] : 0;

                    aVar2Data2[row][col] = (aFreqData2[row] > aFeatureLowers[col + 3]
                            && aFreqData2[row] < aFeatureUppers[col + 3]) ? measureData2.dPwrSpectrum[row] : 0;

                    aVar2Data3[row][col] = (aFreqData3[row] > aFeatureLowers[col + 3]
                            && aFreqData3[row] < aFeatureUppers[col + 3]) ? measureData3.dPwrSpectrum[row] : 0;
                }
            }

            // 측정 raw 데이터와 Var2 연산 결과 생성
            // - Feature별 결과 데이터
            // - 1X ~ VPF, 1/2X, 1/3X, 0.38_0.48X, 2xLF, LF, MaxFreq, CrF, HarmonicSum :
            // SQRT(SUMSQ(rawData))
            // - Noise Floor : rawData의 합이 0보다 작으면 0, 0보다 크면 Percentile(range 참조)
            // - Overall RMS : SQRT(SUMSQ(rawData) / 1.5)
            double[] tmpData = new double[Constants.FREQ_ELE];

            // Data1에 대한 분석 결과
            for (int col = 0; col < Constants.FEATURE_COUNT; col++) {
                for (int row = 0; row < Constants.FREQ_ELE; row++) {
                    if (row == 45)
                        row = row;
                    tmpData[row] = (double) aVar2Data1[row][col];
                }

                if (col == 14) // Noise Floor 1
                    resultMatrix2.aData1[col + 3] = fnNoiseFloor(tmpData, aFeatureRanges[17]);
                else if (col == 15) // Noise Floor 2
                    resultMatrix2.aData1[col + 3] = fnNoiseFloor(tmpData, aFeatureRanges[18]);
                else if (col == 21) // RMS인 경우
                    resultMatrix2.aData1[col + 3] = svsCommon.fnSQRT_SUMSQ(tmpData, true);
                else // A>R, R<A, OA RMS 이외의 경우
                    resultMatrix2.aData1[col + 3] = svsCommon.fnSQRT_SUMSQ(tmpData, false);
            }

            // Data2에 대한 분석 결과
            Arrays.fill(tmpData, 0);
            for (int col = 0; col < Constants.FEATURE_COUNT; col++) {
                for (int row = 0; row < Constants.FREQ_ELE; row++)
                    tmpData[row] = (double) aVar2Data2[row][col];

                if (col == 14) // Noise Floor 1
                    resultMatrix2.aData2[col + 3] = fnNoiseFloor(tmpData, aFeatureRanges[17]);
                else if (col == 15) // Noise Floor 2
                    resultMatrix2.aData2[col + 3] = fnNoiseFloor(tmpData, aFeatureRanges[18]);
                else if (col == 21) // RMS인 경우
                    resultMatrix2.aData2[col + 3] = svsCommon.fnSQRT_SUMSQ(tmpData, true);
                else // A>R, R<A, OA RMS 이외의 경우
                    resultMatrix2.aData2[col + 3] = svsCommon.fnSQRT_SUMSQ(tmpData, false);
            }

            // Data3에 대한 분석 결과
            Arrays.fill(tmpData, 0);
            for (int col = 0; col < Constants.FEATURE_COUNT; col++) {
                for (int row = 0; row < Constants.FREQ_ELE; row++)
                    tmpData[row] = (double) aVar2Data3[row][col];

                if (col == 14) // Noise Floor 1
                    resultMatrix2.aData3[col + 3] = fnNoiseFloor(tmpData, aFeatureRanges[17]);
                else if (col == 15) // Noise Floor 2
                    resultMatrix2.aData3[col + 3] = fnNoiseFloor(tmpData, aFeatureRanges[18]);
                else if (col == 21) // RMS인 경우
                    resultMatrix2.aData3[col + 3] = svsCommon.fnSQRT_SUMSQ(tmpData, true);
                else // A>R, R<A, OA RMS 이외의 경우
                    resultMatrix2.aData3[col + 3] = svsCommon.fnSQRT_SUMSQ(tmpData, false);
            }

            // Data1, 2, 3의 Axial > Radial, Radial > Axial 값 추출
            // - Data1, 2가 Radial 설정이고, Data3가 Axial 설정이라는 전제 조건임
            double dSum_Data1_1x5x, dSum_Data2_1x5x, dSum_Data3_1x5x;
            double dSQRT_Data1, dSQRT_Data2, dSQRT_Data3;
            dSum_Data1_1x5x = dSum_Data2_1x5x = dSum_Data3_1x5x = 0;
            dSQRT_Data1 = dSQRT_Data2 = dSQRT_Data3 = 0;
            double[] aData1_1x5x = new double[5];
            double[] aData2_1x5x = new double[5];
            double[] aData3_1x5x = new double[5];

            for (int i = 0; i < 5; i++) {
                dSum_Data1_1x5x += resultMatrix2.aData1[i + 3];
                dSum_Data2_1x5x += resultMatrix2.aData3[i + 3];
                dSum_Data3_1x5x += resultMatrix2.aData3[i + 3];
                aData1_1x5x[i] = resultMatrix2.aData1[i + 3];
                aData2_1x5x[i] = resultMatrix2.aData2[i + 3];
                aData3_1x5x[i] = resultMatrix2.aData3[i + 3];
            }

            dSQRT_Data1 = svsCommon.fnSQRT_SUMSQ(aData1_1x5x, false);
            dSQRT_Data2 = svsCommon.fnSQRT_SUMSQ(aData2_1x5x, false);
            dSQRT_Data3 = svsCommon.fnSQRT_SUMSQ(aData3_1x5x, false);

            // - Data1의 Axial > Radial 값은
            // . Data1의 SUM(1x..5x) < 0이거나 Data3의 SUM(1x..5x) < 0 이면, 0
            // . 그렇지 않으면, SQRT(SUMSQ(Data3의 1x..5x) - SQRT(SUMSQ(Data1의 1x..5x)
            if (dSum_Data1_1x5x <= 0 || dSum_Data3_1x5x <= 0)
                resultMatrix2.aData1[0] = 0;
            else
                resultMatrix2.aData1[0] = dSQRT_Data3 - dSQRT_Data1;

            // - Data2의 Axial > Radial 값은
            // . Data2의 SUM(1x..5x) < 0이거나 Data3의 SUM(1x..5x) < 0 이면, 0
            // . 그렇지 않으면, SQRT(SUMSQ(Data3의 1x..5x) - SQRT(SUMSQ(Data2의 1x..5x)
            if (dSum_Data2_1x5x <= 0 || dSum_Data3_1x5x <= 0)
                resultMatrix2.aData2[0] = 0;
            else
                resultMatrix2.aData2[0] = dSQRT_Data3 - dSQRT_Data2;

            // - Data3의 Axial > Radial은 Skip
            resultMatrix2.aData3[0] = 0;

            // - Data1의 Radial > Axial 값은
            // . Data1의 SUM(1x..5x) < 0이거나 Data3의 SUM(1x..5x) < 0 이면, 0
            // . 그렇지 않으면, SQRT(SUMSQ(Data1의 1x..5x) - SQRT(SUMSQ(Data3의 1x..5x)
            if (dSum_Data1_1x5x <= 0 || dSum_Data3_1x5x <= 0)
                resultMatrix2.aData1[1] = 0;
            else
                resultMatrix2.aData1[1] = dSQRT_Data1 - dSQRT_Data3;

            // - Data2의 Radial > Axial 값은
            // . Data2의 SUM(1x..5x) < 0이거나 Data3의 SUM(1x..5x) < 0 이면, 0
            // . 그렇지 않으면, SQRT(SUMSQ(Data2의 1x..5x) - SQRT(SUMSQ(Data3의 1x..5x)
            if (dSum_Data2_1x5x <= 0 || dSum_Data3_1x5x <= 0)
                resultMatrix2.aData2[1] = 0;
            else
                resultMatrix2.aData2[1] = dSQRT_Data2 - dSQRT_Data3;

            // - Data3의 Radial > Axial은 Skip
            resultMatrix2.aData3[1] = 0;

            // HORZ/VERT
            resultMatrix2.aData1[2] = resultMatrix2.aData1[3];
            resultMatrix2.aData2[2] = resultMatrix2.aData2[3];
            resultMatrix2.aData3[2] = resultMatrix2.aData3[3];

            // Data1, 2, 3에 대한 MAX 추출
            double fMax;
            for (int col = 0; col < Constants.FEATURE_COUNT + 3; col++) // A>R, R<A, HORZ/VERT 포함
            {
                if (col != 2) {
                    fMax = Math.max(resultMatrix2.aData1[col], resultMatrix2.aData2[col]);
                    resultMatrix2.aDataMax[col] = Math.max(fMax, resultMatrix2.aData3[col]);
                } else // HORZ/VERT
                {
                    if (resultMatrix2.aData1[col] <= 0 || resultMatrix2.aData2[col] <= 0
                            || resultMatrix2.aData3[col] <= 0)
                        resultMatrix2.aDataMax[col] = 0;
                    else {
                        if (diagVar1.nEquipType == 1) // Horizontal (BB&OH)
                            resultMatrix2.aDataMax[col] = resultMatrix2.aData1[col];
                        else if (diagVar1.nEquipType == 2) // Vertical (VS)
                            resultMatrix2.aDataMax[col] = resultMatrix2.aData3[col];
                        else
                            resultMatrix2.aDataMax[col] = 0;
                    }
                }
            }

            // Data1, 2, 3에 대한 Median 추출
            for (int col = 0; col < Constants.FEATURE_COUNT + 3; col++) // A>R, R<A, HORZ/VERT 포함
            {
                if (col != 2) {
                    resultMatrix2.aDataMed[col] = svsCommon.fnMedian(resultMatrix2.aData1[col],
                            resultMatrix2.aData2[col], resultMatrix2.aData3[col]);
                } else // HORZ/VERT
                {
                    if (diagVar1.nEquipType == 1) // Horizontal (BB&OH)
                        resultMatrix2.aDataMed[col] = Math.max(resultMatrix2.aData1[col], resultMatrix2.aData1[col]);
                    else if (diagVar1.nEquipType == 2) // Vertical (VS)
                        resultMatrix2.aDataMed[col] = Math.max(resultMatrix2.aData2[col], resultMatrix2.aData3[col]);
                    else
                        resultMatrix2.aDataMed[col] = 0;
                }
            }

            // Matrix2의 Result : Rank 계산 활용 목적
            // - 각 Feature별
            // - MAX(Data1 Feature 값..Data3 Feature 값)^2 / MAX(Data1 RMS..Data3 RMS)^2
            for (int i = 0; i < Constants.FEATURE_COUNT + 3; i++) {
                if (i != 2) {
                    resultMatrix2.aResult[i] = fnMatrix2Result(resultMatrix2.aData1[i], resultMatrix2.aData2[i],
                            resultMatrix2.aData3[i], resultMatrix2.aData1[24], resultMatrix2.aData2[24],
                            resultMatrix2.aData3[24]);
                } else // HORZ/VERT
                {
                    resultMatrix2.aResult[i] = fnMatrix2HVResult(resultMatrix2.aData1, resultMatrix2.aData2,
                            resultMatrix2.aData3);
                }
            }

            // Matrix2의 result와 Feature 가중치 테이블(cause x feature, 21 x 25)과 연산(곱셈)
            for (int row = 0; row < nCauseCount; row++) {
                for (int col = 0; col < Constants.FEATURE_COUNT; col++) {
                    tableResult[row][col] = resultMatrix2.aResult[col] * tableFeature[row][col];
                }
            }

            // RANK 추출을 위한 SUM & Ratio 테이블 추출
            double[][] resultDiagnosis = new double[nCauseCount][3]; // 21(cause) x 3(rank, sum, ratio) 배열
            // - SUM 추출
            double dSum = 0;
            for (int row = 0; row < nCauseCount; row++) {
                dSum = 0;
                // SUM = tableResult[row]열의 합 ^ 11
                for (int col = 0; col < Constants.FEATURE_COUNT; col++) // Sum, Ratio 추출, Rank(col : 0)는 다음에 계산
                {
                    dSum += tableResult[row][col];
                }
                resultDiagnosis[row][1] = Math.pow(dSum, 11);
            }

            // - Ratio 추출
            double dTotalSum = 0; // Ratio 연산을 위해 생성
            for (int row = 0; row < nCauseCount; row++)
                dTotalSum += resultDiagnosis[row][1];
            dTotalSum = Math.abs(dTotalSum);

            for (int row = 0; row < nCauseCount; row++) {
                resultDiagnosis[row][2] = resultDiagnosis[row][1] / dTotalSum;
            }

            // - Rank 추출
            for (int i = 0; i < nCauseCount; i++) {
                resultDiagnosis[i][0] = 1;
                for (int j = 0; j < nCauseCount; j++) {
                    // 비교 후 값이 작으면 1씩 증가
                    if (resultDiagnosis[i][1] < resultDiagnosis[j][1]) {
                        resultDiagnosis[i][0]++; // rank 기록
                    }
                }
            }

            if (Constants.DEBUG) { // 각 중간 연산 과정 확인용
                String strTmp;

                // Variable2 기반
                for (int row = 0; row < Constants.FREQ_ELE; row++) {
                    for (int col = 0; col < Constants.FEATURE_COUNT; col++) {
                        if (col < 24 && aVar2Data1[row][col] > 0) {
                            System.out.println(
                                    "[DEBUG] aVar2Data1[" + row + ", " + col + "] : " + (aVar2Data1[row][col] * 1000));
                        }
                    }
                }

                for (int row = 0; row < Constants.FREQ_ELE; row++) {
                    for (int col = 0; col < Constants.FEATURE_COUNT; col++) {
                        if (col < 24 && aVar2Data2[row][col] > 0) {
                            System.out.println(
                                    "[DEBUG] aVar2Data2[" + row + ", " + col + "] : " + (aVar2Data2[row][col] * 1000));
                        }
                    }
                }

                for (int row = 0; row < Constants.FREQ_ELE; row++) {
                    for (int col = 0; col < Constants.FEATURE_COUNT; col++) {
                        if (col < 24 && aVar2Data3[row][col] > 0) {
                            System.out.println(
                                    "[DEBUG] aVar2Data3[" + row + ", " + col + "] : " + (aVar2Data3[row][col] * 1000));
                        }
                    }
                }

                //// 각 측정 데이터별 분석 결과 데이터
                // strTmp = string.Format("result DATA1 : ");
                // for (int col = 0; col < Constants.FREQ_ELE; col++)
                // {
                // strTmp += string.Format("{0:F2}", )
                // }
            }

            // mainform.diagnosisForm.fnDisplayResult(aFeatureValues, aFeatureRanges,
            // aFeatureLowers, aFeatureUppers, resultMatrix2, resultDiagnosis);
            int i = 2;
        } catch (Exception ex) {
            System.out.println("[Exception: fnMakeMatrix2] ");
            ex.printStackTrace();
        }

        return resultMatrix2;
    }

    private double fnNoiseFloor(double[] data, double dPercentile) {
        double dNoiseFloor = 0;
        double dSum = 0;

        try {
            for (int i = 0; i < data.length; i++)
                dSum += data[i];

            if (dSum > 0) {
                dNoiseFloor = svsCommon.fnPercentile(data, dPercentile);
                // dNoiseFloor = Statistics.Percentile(data, 35);
                // dNoiseFloor = Statistics.Quantile(data, 0.35);
            }
        } catch (Exception ex) {
            System.out.println("[Exception: fnNoiseFloor] " + ex.getMessage());
        }

        return dNoiseFloor;
    }

    /// <summary>
    /// MAX(feature1..feature2)^2 / MAX(rms1..rms3)^2 계산
    /// </summary>
    /// <param name="feature1"></param>
    /// <param name="feature2"></param>
    /// <param name="feature3"></param>
    /// <param name="rms1"></param>
    /// <param name="rms2"></param>
    /// <param name="rms3"></param>
    /// <returns></returns>
    private double fnMatrix2Result(double feature1, double feature2, double feature3, double rms1, double rms2,
            double rms3) {
        double result = 0;

        double dMaxFeature = 0;
        double dMaxRMS = 0;

        dMaxFeature = Math.max(feature1, feature2);
        dMaxFeature = Math.max(dMaxFeature, feature3);

        dMaxRMS = Math.max(rms1, rms2);
        dMaxRMS = Math.max(dMaxRMS, rms3);

        result = Math.pow(dMaxFeature, 2) / Math.pow(dMaxRMS, 2);

        return result;
    }

    /// <summary>
    /// HORZ/VERT 결과 추출
    /// </summary>
    /// <param name="aData1"></param>
    /// <param name="aData2"></param>
    /// <param name="aData3"></param>
    /// <returns></returns>
    private double fnMatrix2HVResult(double[] aData1, double[] aData2, double[] aData3) {
        double result = 0;

        double[] dTmp = new double[24]; // 3x8, Data1~3의 1x~12x 값 임시 저장용
        System.arraycopy(aData1, 3, dTmp, 0, 8);
        System.arraycopy(aData2, 3, dTmp, 8, 8);
        System.arraycopy(aData3, 3, dTmp, 16, 8);

        // double dMax1x_12x = dTmp.Max();
        double dMax1x_12x = SVSCommon.max(dTmp);

        double[] dTmp1x = new double[3];
        dTmp1x[0] = aData1[2];
        dTmp1x[1] = aData2[2];
        dTmp1x[2] = aData3[2];
        double dMax1x = SVSCommon.max(dTmp1x);

        if (dMax1x_12x == dMax1x) {
            if (dTmp1x[1] == 0 || dTmp1x[2] == 0)
                result = 0;
            else {
                if (dTmp1x[2] / dTmp1x[1] < 1.5)
                    result = 0;
                else {
                    if (dTmp1x[2] / dTmp1x[1] > 2)
                        result = 1;
                    else
                        result = dTmp1x[2] / dTmp1x[1] / 2;
                }
            }
        } else
            result = 0;

        return result;
    }
}
// }
