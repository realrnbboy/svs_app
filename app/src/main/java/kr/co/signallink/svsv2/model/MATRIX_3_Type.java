package kr.co.signallink.svsv2.model;

    // MATRIX 3
    // - MATRIX 1 * MATRIX 2(행렬 곱)을 통해 도출
    // - Cause(MATRIX 4)들에 대한 Rank, Sum, Ratio 연산
    //[StructLayout(LayoutKind.Sequential, Pack = 1)]
    public class MATRIX_3_Type
    {
        public int nCause;  // Cause 수 만큼
        public int[] fRanks;
        public float[] fSums;
        public float[] fRatio;
    }