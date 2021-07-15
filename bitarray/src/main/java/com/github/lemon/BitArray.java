package com.github.lemon;


import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 一个比特数组，可操作的每一个基本元素为一个比特位。
 *
 * @author Hami Lemon
 * @version v1.0
 */
public class BitArray implements Cloneable, Serializable {
    /**
     * bits数组中每个元素的比特数
     */
    private final static int PER_BIT = 8;
    /**
     * PER_BIT是 2 PER_BIT_OF_POWER 次幂
     */
    private final static int PER_BIT_OF_POWER = 3;

    /**
     * 实际使用的比特位数
     */
    private int size;

    private final byte[] bits;

    private interface Operation {
        int operate(byte v1, byte v2);
    }

    /**
     * 默认创建 4 个字节即 32 个比特位
     */
    public BitArray() {
        this.bits = new byte[4];
        this.size = 4 << PER_BIT_OF_POWER;
    }

    /**
     * 按照指定的长度初始化一定比特位长度的BitArray
     *
     * @param length 初始化的比特位长度
     */
    public BitArray(int length) {
        if (length < 0) throw new IllegalArgumentException("长度不能小于0");
        //由于数组是一个byte数组，所以实际创建的比特位长度 略大于 length（也可能等于） 且为8的倍数
        this.bits = new byte[(length - 1 >> PER_BIT_OF_POWER) + 1];
        this.size = length;
    }

    /**
     * 根据传入的字节数组创建BitArray
     *
     * @param bits 字节数组
     */
    public BitArray(byte[] bits) {
        this.bits = bits;
        //字节数 * 8
        this.size = bits.length << PER_BIT_OF_POWER;
    }

    /**
     * 按照 UTF-8 获取字符串对应的二进制数，并据此创建BitArray
     *
     * @param str 一个字符串
     */
    public BitArray(String str) {
        this(str, StandardCharsets.UTF_8);
    }

    /**
     * 按照指定的字符集获取字符串对应的二进制数，并据此创建BitArray
     *
     * @param str     一个字符串
     * @param charset 获取字符串对应二进制数所参照的字符集
     */
    public BitArray(String str, Charset charset) {
        this(str.getBytes(charset));
    }

    /**
     * 将某一比特位置 1
     *
     * @param pos 比特位的索引，从 0 开始
     */
    public void active(int pos) {
        set(pos, true);
    }

    public void active(int index, int bitIndex) {
        set(index, bitIndex, true);
    }

    /**
     * 将某一比特位置 0
     *
     * @param pos 比特位的索引，从 0 开始
     */
    public void passive(int pos) {
        set(pos, false);
    }

    public void passive(int index, int bitIndex) {
        set(index, bitIndex, false);
    }

    /**
     * 根据传入的布尔值设置对应比特位的值，为true时置1，为false时置0
     *
     * @param pos 比特位的索引，从 0 开始
     * @param val 设置的值，为true时设为1，为false时设为0
     */
    public void set(int pos, boolean val) {
        //计算这一个比特位是第几个字节，从0开始
        int index = pos >> PER_BIT_OF_POWER;
        //计算这一个比对位对应这一个字节中的第几位，从0开始
        int bitIndex = pos & (PER_BIT - 1);
        set(index, bitIndex, val);
    }

    /**
     * 设置第几个字节上的第几个比特位的值
     *
     * @param index    第几个字节，从 0 开始
     * @param bitIndex 该字节上的第几个比特位，[0,7]
     * @param val      设置的值，true为1，false为0
     */

    public void set(int index, int bitIndex, boolean val) {
        if (bitIndex < 0 || bitIndex > 7) throw new IllegalArgumentException("错误的比特位索引:" + bitIndex);
        if (val)
            bits[index] |= 1 << bitIndex;
        else
            bits[index] &= ~(1 << bitIndex);
    }

    public void setNthByte(int nth, byte val) {
        bits[nth] = val;
    }

    public void setNthByte(int nth, int val) {
        setNthByte(nth, (byte) val);
    }


    /**
     * 获取对应比特位上的值，返回true时为1，返回false时为0
     *
     * @param pos 比特位的索引，从0开始
     * @return 对应的值，true为1，false为0
     */
    public boolean get(int pos) {
        int index = pos >> PER_BIT_OF_POWER;
        int bitIndex = pos & (PER_BIT - 1);
        return getFromByte(bitIndex, bits[index]);
    }

    /**
     * 从一个byte中获取某一个比特位上的值
     *
     * @param bitIndex 索引
     * @param b        byte数据
     * @return true为1，false为0
     */
    private boolean getFromByte(int bitIndex, byte b) {
        return (b & (1 << bitIndex)) != 0;
    }

    public byte getNthByte(int nth) {
        if (nth > bits.length) throw new IllegalArgumentException("超过最大的字节数");
        return bits[nth];
    }

    public int getByteSize() {
        return bits.length;
    }

    public int getBitSize() {
        return size;
    }

    private void cal(BitArray bitArray, Operation operation) {
        int fByteSize = bits.length;
        int sByteSize = bitArray.getByteSize();

        for (int i = 0; i < fByteSize; i++) {
            byte f = bits[i], s = 0x00;
            if (i < sByteSize) s = bitArray.getNthByte(i);

            bits[i] = (byte) operation.operate(f, s);
        }
    }

    /**
     * 将当前BitArray和给定的BitArray进行与运算，对每一个二进制位，都为1时结果为1
     * 例：<pre>
     *     10001111 and
     *     01001010
     *   = 00001010</pre>
     *
     * @param bitArray 一个BitArray
     */
    public void and(BitArray bitArray) {
        cal(bitArray, (v1, v2) -> v1 & v2);
    }

    /**
     * 将当前BitArray和给定的BitArray进行或运算，对每一个二进制位，只有都为0时结果才为0
     * 例：<pre>
     *     10001111 or
     *     01001010
     *   = 11001111</pre>
     *
     * @param bitArray 一个BitArray
     */
    public void or(BitArray bitArray) {
        cal(bitArray, ((v1, v2) -> v1 | v2));
    }

    /**
     * 将当前BitArray进行非运算，对每一个二进制位，按位取反。
     * 例：<pre>
     *     10001111 not
     *   = 01110000</pre>
     */
    public void not() {
        BitArray r = new BitArray(size);
        byte[] bytes = bits;
        for (int i = 0; i < bytes.length; i++) {
            r.setNthByte(i, ~bytes[i]);
        }

    }

    /**
     * 将当前BitArray和给定的BitArray进行或非运算，对每一个二进制位，都为0时结果为1，等价于对或运算取反。
     * 例：<pre>
     *     10001111 nor
     *     01001010
     *   = 00110000</pre>
     *
     * @param bitArray 一个BitArray
     */
    public void nor(BitArray bitArray) {
        cal(bitArray, (v1, v2) -> ~(v1 | v2));
    }

    /**
     * 将当前BitArray和给定的BitArray进行异或运算，对每一个二进制位，两个值不同时结果为1
     * 例：<pre>
     *     10001111 xor
     *     01001010
     *   = 11000101</pre>
     *
     * @param bitArray 一个BitArray
     */
    public void xor(BitArray bitArray) {
        cal(bitArray, (v1, v2) -> v1 ^ v2);
    }

    /**
     * 将当前BitArray和给定的BitArray进行异或非运算，对每一个二进制位，两个值相同时结果为1，等价于对异或运算取反。
     * 例：<pre>
     *     10001111 xnor
     *     01001010
     *   = 00111010</pre>
     *
     * @param bitArray 一个BitArray
     */
    public void notXor(BitArray bitArray) {
        cal(bitArray, (v1, v2) -> ~(v1 ^ v2));
    }

    /**
     * 将当前BitArray和给定的BitArray进行与非运算，对每一个二进制位，两个值不都为1时结果为1，等价于对与运算求反。
     * 例：<pre>
     *     10001111 nand
     *     01001010
     *   = 11110101</pre>
     *
     * @param bitArray 一个BitArray
     */
    public void notAnd(BitArray bitArray) {
        cal(bitArray, (v1, v2) -> ~(v1 & v2));
    }

    @Override
    public String toString() {
        int len;
        byte[] bytes = bits;
        StringBuilder builder = new StringBuilder((len = bytes.length) << 1);
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < 8; j++) {
                builder.append(getFromByte(j, bytes[i]) ? '1' : '0');
            }
            builder.append(" ");
        }
        /*
         在bits数组中，低字节在前，高字节在后（注意是字节，不是比特位）
         例如：00000011 00000010 00000001 00000000在数组中的存储顺序为
              00000000 00000001 00000010 00000011
         */
        return builder.reverse()
                //删除开头的空格
                .delete(0, 1)
                .toString();
    }
}
