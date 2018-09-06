package com.emoth.emothcipher.sm3;

import android.util.Log;

import com.emoth.emothcipher.util.OperationUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * 注： SM3 算法中 <<< 为循环左移，而不是逻辑左移或算术左移
 * Created by EmothWei on 2018/9/4.
 */
public class SM3 {

    /**
     * 4.1 初始值 IV
     */
    private static final BigInteger IV = new BigInteger(
            "7380166f4914b2b9172442d7da8a0600a96f30bc163138aae38dee4db0fb0e4e", 16);
    /**
     * 4.2 常量 T 随 j 的变化而取不同的值
     */
    private static final int T(int j) {
        if (j >= 0 && j < 16) {
            return 0x79cc4519;
        } else if (j >=16 && j < 64) {
            return 0x7a879d8a;
        } else {
            throw new IndexOutOfBoundsException("Constant index invalid: Must between 0 and 64");
        }
    }

    /**
     * 4.3 布尔函数 FF 参数为 int
     * @param X
     * @param Y
     * @param Z
     * @param j
     * @return
     */
    private static final int FF(int X,int Y,int Z,int j) {
        if (j >= 0 && j < 16) {
            return X ^ Y ^ Z;
        } else if (j >= 16 && j < 64) {
            return (X & Y) | (X & Z) | (Y & Z);
        } else {
            throw new IndexOutOfBoundsException("Constant index invalid: Must between 0 and 64");
        }
    }

    /**
     * 4.3 布尔函数 GG 参数为 int
     * @param X
     * @param Y
     * @param Z
     * @param j
     * @return
     */
    private static final int GG(int X, int Y, int Z, int j) {
        if (j >= 0 && j < 16) {
            return X ^ Y ^ Z;
        } else if (j >= 16 && j < 64) {
            return (X & Y) | (~X & Z);
        } else {
            throw new IndexOutOfBoundsException("Constant index invalid: Must between 0 and 64");
        }
    }

    /**
     * 4.4 置换函数 P0 参数为 int 循环左移
     * @param X
     * @return
     */
    private static final int P0(int X) {
        return X ^ Integer.rotateLeft(X, 9)
                ^ Integer.rotateLeft(X, 17);
    }

    /**
     * 4.4 置换函数 P1 参数为 int 循环左移
     * @param X
     * @return
     */
    private static final int P1(int X) {
        return X ^ Integer.rotateLeft(X, 15)
                ^ Integer.rotateLeft(X, 23);
    }

    /**
     * 5.2 填充函数
     * @param m
     * @return
     * @throws IOException
     */
    private static byte[] padding(byte[] m) throws IOException {
        // 消息 m 的长度
        long l = m.length * 8;
        // '0'的长度
        long k = 448 - (l + 1) % 512;
        if (k < 0) {
            k = k + 512;
        }
        // ByteArrayOutputStream 存储填充后的数据
        ByteArrayOutputStream res = new ByteArrayOutputStream();
        res.write(m);
        final byte[] messageEndPadding = {(byte) 0b10000000};
        final byte[] zeroPadding = {(byte) 0b00000000};
        res.write(messageEndPadding);
        long i = k - 7;
        while (i > 0) {
            res.write(zeroPadding);
            i -= 8;
        }
        res.write(OperationUtil.longToBytes(l));
        return res.toByteArray();
    }

    /**
     * 5.3.3 压缩函数
     * @param Vi 256bit
     * @param Bi 填充后的消息分组
     * @return
     * @throws IOException
     */
    private static byte[] CF(byte[] Vi, byte[] Bi) throws IOException {
        int A, B, C, D, E, F, G, H;
        A = OperationUtil.byteToInt(Vi, 0);
        B = OperationUtil.byteToInt(Vi, 1);
        C = OperationUtil.byteToInt(Vi, 2);
        D = OperationUtil.byteToInt(Vi, 3);
        E = OperationUtil.byteToInt(Vi, 4);
        F = OperationUtil.byteToInt(Vi, 5);
        G = OperationUtil.byteToInt(Vi, 6);
        H = OperationUtil.byteToInt(Vi, 7);

        // 5.3.2 消息扩展 W[68] W'[64]
        int[] W = new int[68];
        int[] W1 = new int[64];
        // 将消息分组 Bi 划分为 16 个字 W0 W1 ... W15
        for (int i = 0; i < 16; i++) {
            W[i] = OperationUtil.byteToInt(Bi, i);
        }
        for (int i = 16; i < 68; i++) {
            W[i] = P1(W[i - 16] ^ W[i - 9] ^ Integer.rotateLeft(W[i - 3], 15))
                    ^ Integer.rotateLeft(W[i - 13], 7) ^ W[i - 6];
        }
        for (int i = 0; i < 64; i++) {
            W1[i] = W[i] ^ W[i + 4];
        }
        // 消息扩展完成


        // 中间变量
        int SS1, SS2, TT1, TT2;
        for (int i = 0; i < 64; i++) {
            SS1 = Integer.rotateLeft((Integer.rotateLeft(A, 12) + E +
                    Integer.rotateLeft(T(i), i)), 7);
            SS2 = SS1 ^ Integer.rotateLeft(A, 12);
            TT1 = FF(A, B, C, i) + D + SS2 + W1[i];
            TT2 = GG(E, F, G, i) + H + SS1 + W[i];
            D = C;
            C = Integer.rotateLeft(B, 9);
            B = A;
            A = TT1;
            H = G;
            G = Integer.rotateLeft(F, 19);
            F = E;
            E = P0(TT2);
        }
        byte[] v = OperationUtil.byteToBytes(A, B, C, D, E, F, G, H);
        for (int i = 0; i < v.length; i++) {
            v[i] = (byte) (v[i] ^ Vi[i]);
        }
        return v;
    }

    /**
     * 5.3.3 压缩函数 线程安全
     * @param Vi 256bit
     * @param Bi 填充后的消息分组
     * @return
     * @throws IOException
     */
    private static byte[] CFThread(byte[] Vi, byte[] Bi) throws IOException {
        int A, B, C, D, E, F, G, H;
        A = OperationUtil.byteToIntThread(Vi, 0);
        B = OperationUtil.byteToIntThread(Vi, 1);
        C = OperationUtil.byteToIntThread(Vi, 2);
        D = OperationUtil.byteToIntThread(Vi, 3);
        E = OperationUtil.byteToIntThread(Vi, 4);
        F = OperationUtil.byteToIntThread(Vi, 5);
        G = OperationUtil.byteToIntThread(Vi, 6);
        H = OperationUtil.byteToIntThread(Vi, 7);

        // 5.3.2 消息扩展 W[68] W'[64]
        int[] W = new int[68];
        int[] W1 = new int[64];
        // 将消息分组 Bi 划分为 16 个字 W0 W1 ... W15
        for (int i = 0; i < 16; i++) {
            W[i] = OperationUtil.byteToInt(Bi, i);
        }
        for (int j = 16; j < 68; j++) {
            W[j] = P1(W[j - 16] ^ W[j - 9] ^ (W[j - 3] << 15))
                    ^ (W[j - 13] << 7) ^ W[j - 6];
        }
        for (int j = 0; j < 64; j++) {
            W1[j] = W[j] ^ W[j + 4];
        }
        // 消息扩展完成


        // 中间变量
        int SS1, SS2, TT1, TT2;
        for (int j = 0; j < 64; j++) {
            SS1 = ((A << 12) + E + (T(j) << j)) << 7;
            SS2 = SS1 ^ (A << 12);
            TT1 = FF(A, B, C, j) + D + SS2 + W1[j];
            TT2 = GG(E, F, G, j) + H + SS1 + W[j];
            D = C;
            C = B << 9;
            B = A;
            A = TT1;
            H = G;
            G = F << 19;
            F = E;
            E = P0(TT2);
        }
        byte[] v = OperationUtil.byteToBytes(A, B, C, D, E, F, G, H);
        for (int i = 0; i < v.length; i++) {
            v[i] = (byte) (v[i] ^ Vi[i]);
        }
        return v;
    }

    /**
     * 5.3.1 迭代过程
     * @param source
     * @return
     * @throws IOException
     */
    private static byte[] hash(byte[] source) throws IOException {
        byte[] m1 = padding(source);
        int n = m1.length / (512 / 8);
        byte[] b;
        byte[] vi = IV.toByteArray();
        byte[] vi1 = null;
        for (int i = 0; i < n; i++) {
            b = Arrays.copyOfRange(m1, i * 64, (i + 1) * 64);
            vi1 = CF(vi, b);
            vi = vi1;
        }
        return vi1;
    }

    /**
     * 5.3.1 迭代过程 线程安全
     * @param source
     * @return
     * @throws IOException
     */
    private static byte[] hashThread(byte[] source) throws IOException {
        byte[] m1 = padding(source);
        int n = m1.length / (512 / 8);
        byte[] b;
        byte[] vi = IV.toByteArray();
        byte[] vi1 = null;
        for (int i = 0; i < n; i++) {
            b = Arrays.copyOfRange(m1, i * 64, (i + 1) * 64);
            vi1 = CFThread(vi, b);
            vi = vi1;
        }
        return vi1;
    }


    public static String sm3(String source) throws IOException {
        return OperationUtil.bytesToHexString(SM3.hash(source.getBytes()));
    }

    public static String sm3Thread(String source) throws IOException {
        return OperationUtil.bytesToHexStringThread(SM3.hashThread(source.getBytes()));
    }

}
