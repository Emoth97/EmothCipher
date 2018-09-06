package com.emoth.emothcipher.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by EmothWei on 2018/9/4.
 */
public class OperationUtil {

    /**
     * 十六进制字符
     */
    private static char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
            '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * 字转 int 线程安全
     * 多线程使用时为保证线程安全，使用 StringBuffer
     * @param source
     * @param index
     * @return
     */
    public static int byteToIntThread(byte[] source, int index) {
        StringBuffer str = new StringBuffer("");
        for (int i = 0; i < 4; i++) {
            str.append(hexDigits[(byte) ((source[index * 4 + i] & 0xF0) >> 4)]);
            str.append(hexDigits[(byte) (source[index * 4 + i] & 0x0F)]);
        }
        return Long.valueOf(str.toString(), 16).intValue();
    }

    /**
     * 字转 int 线程不安全
     * @param source
     * @param index
     * @return
     */
    public static int byteToInt(byte[] source, int index) {
        StringBuilder str = new StringBuilder("");
        for (int i = 0; i < 4; i++) {
            str.append(hexDigits[(byte) ((source[index * 4 + i] & 0xF0) >> 4)]);
            str.append(hexDigits[(byte) (source[index * 4 + i] & 0x0F)]);
        }
        return Long.valueOf(str.toString(), 16).intValue();
    }

    /**
     * long 转 byte[]
     * @param l
     * @return
     */
    public static byte[] longToBytes(long l) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (l >>> ((7 - i) * 8));
        }
        return bytes;
    }

    /**
     * int 转 byte[]
     * @param l
     * @return
     */
    public static byte[] intToBytes(int l) {
        byte[] bytes = new byte[4];

        bytes[0] = (byte) ((l >>> 24) & 0xFF);
        bytes[1] = (byte) ((l >>> 16) & 0xFF);
        bytes[2] = (byte) ((l >>> 8) & 0xFF);
        bytes[3] = (byte) (l & 0xFF);
        return bytes;
    }

    /**
     * byte 转 byte[]
     * @param a
     * @param b
     * @param c
     * @param d
     * @param e
     * @param f
     * @param g
     * @param h
     * @return
     * @throws IOException
     */
    public static byte[] byteToBytes(int a, int b, int c, int d, int e, int f,
                                      int g, int h) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        baos.write(intToBytes(a));
        baos.write(intToBytes(b));
        baos.write(intToBytes(c));
        baos.write(intToBytes(d));
        baos.write(intToBytes(e));
        baos.write(intToBytes(f));
        baos.write(intToBytes(g));
        baos.write(intToBytes(h));
        return baos.toByteArray();
    }

    /**
     * byte 转 string
     * @param b
     * @return
     */
    public static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return ""+hexDigits[d1] + hexDigits[d2];
    }

    /**
     * byte[] 转 string
     * @param source
     * @return
     */
    public static String bytesToHexString(byte[] source) {
        StringBuilder resultSb = new StringBuilder();
        for (int i = 0; i < source.length; i++) {
            resultSb.append(byteToHexString(source[i]));
        }
        return resultSb.toString();
    }

    /**
     * byte[] 转 string 线程安全
     * @param source
     * @return
     */
    public static String bytesToHexStringThread(byte[] source) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < source.length; i++) {
            resultSb.append(byteToHexString(source[i]));
        }
        return resultSb.toString();
    }


}
