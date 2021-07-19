package com.github.lemon;


import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 一个比特数组，可操作的每一个最小元素为一个比特位。
 * <p>
 * 在此类中使用byte数组保存比特位数据，因此也可以一个字节一个字节的操作数据
 * <p>
 * 注意：在byte数组中低字节的数据在前，高字节的数据在后，（注意这里说的是字节，不是比特位）
 * 例如：<pre>
 *     00000011 00000010 00000001 00000000   在数组中的存储顺序为
 *     00000000 00000001 00000010 00000011</pre>
 *
 * @author Hami Lemon
 * @version v1.0
 */
public class BitArray implements Cloneable, Serializable {
    /**
     * PER_BIT是 2 的 PER_BIT_OF_POWER 次幂
     */
    private final static int PER_BIT_OF_POWER = 3;
    /**
     * bits数组中每个元素的比特数
     */
    private final static int PER_BIT = 1 << PER_BIT_OF_POWER;
    /**
     * 实际使用的比特位数，会根据初始化BitArray设定一个初始值，
     * 而后，当发生扩容操作时，会将该值更新。
     * <p>
     * 例如：当初始化长度为4时，该值为4，然后调用方法<b>active(9)</b>，
     * 则会触发扩容操作，并且该值会更新为9
     * </p>
     */
    private int size;

    //保存数据的数组
    private byte[] bits;

    //执行逻辑运算的操作接口
    @FunctionalInterface
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

    public static BitArray valueOf(byte val) {
        return valueOf(val, 1);
    }

    public static BitArray valueOf(short val) {
        return valueOf(val, 2);
    }

    public static BitArray valueOf(int val) {
        return valueOf(val, 4);
    }

    public static BitArray valueOf(long val) {
        return valueOf(val, 8);
    }

    public static BitArray valueOf(String str) {
        //删除空白符
        str = str.replaceAll("\\s", "");
        int len = str.length();
        BitArray bitArray = new BitArray(len);
        for (int i = len - 1; i >= 0; i--) {
            char c = str.charAt(i);
            int pos = len - 1 - i;
            if (c == '1')
                bitArray.active(pos);
            else if (c == '0')
                bitArray.passive(pos);
            else
                throw new IllegalArgumentException("含有非法字符");
        }
        return bitArray;
    }

    private static BitArray valueOf(long val, int byteNum) {
        BitArray bitArray = new BitArray(byteNum << PER_BIT_OF_POWER);
        bitArray.bits[0] = (byte) val;
        for (int i = 1; i < byteNum; i++) {
            bitArray.bits[i] = (byte) (val = (val >>> PER_BIT));
        }
        return bitArray;
    }

    /**
     * 将某一比特位置 1
     *
     * @param pos 比特位的索引，从 0 开始
     * @return 返回this, 以便链式调用
     */
    public BitArray active(int pos) {
        return set(pos, true);
    }

    /**
     * 将第几个字节的第几位置1
     *
     * @param index    第几个字节，从 0 开始
     * @param bitIndex 该字节的第几个比特位，[0,7]
     * @return 返回this, 以便链式调用
     */
    public BitArray active(int index, int bitIndex) {
        return set(index, bitIndex, true);
    }

    public BitArray activeRange(int start, int end) {
        return setRange(start, end, true);
    }

    /**
     * 将某一比特位置 0
     *
     * @param pos 比特位的索引，从 0 开始
     * @return 返回this, 以便链式调用
     */
    public BitArray passive(int pos) {
        return set(pos, false);
    }

    /**
     * 将第几个字节的第几位置0
     *
     * @param index    第几个字节，从 0 开始
     * @param bitIndex 该字节的第几个比特位，[0,7]
     * @return 返回this, 以便链式调用
     */
    public BitArray passive(int index, int bitIndex) {
        return set(index, bitIndex, false);
    }

    public BitArray passiveRange(int start, int end) {
        return setRange(start, end, false);
    }

    /**
     * 根据传入的布尔值设置对应比特位的值，为true时置1，为false时置0
     *
     * @param pos 比特位的索引，从 0 开始
     * @param val 设置的值，为true时设为1，为false时设为0
     * @return 返回this, 以便链式调用
     */
    public BitArray set(int pos, boolean val) {
        //计算这一个比特位是第几个字节，从0开始
        int index = pos >> PER_BIT_OF_POWER;
        //计算这一个比对位对应这一个字节中的第几位，从0开始
        int bitIndex = pos & (PER_BIT - 1);
        return set(index, bitIndex, val);
    }

    /**
     * 设置第几个字节上的第几个比特位的值
     *
     * @param index    第几个字节，从 0 开始
     * @param bitIndex 该字节上的第几个比特位，<b>[0,7]</b>
     * @param val      设置的值，true为1，false为0
     * @return 返回this, 以便链式调用
     */

    public BitArray set(int index, int bitIndex, boolean val) {
        if (bitIndex < 0 || bitIndex > 7)
            throw new IllegalArgumentException("错误的索引: bitIndex应属于[0,7],但实际为：" + bitIndex);
        if (index >= bits.length) {
            //长度增加到所需的字节数
            resize(index + 1);
        }
        size = Integer.max((index << PER_BIT_OF_POWER) + bitIndex, size);

        if (val)
            bits[index] |= 1 << bitIndex;
        else
            bits[index] &= ~(1 << bitIndex);
        return this;
    }

    public BitArray setRange(int start, int end, boolean val) {
        edgeCheck(start, end);

        //start 和 end 所在的字节位
        int si = start >> PER_BIT_OF_POWER, ei = --end >> PER_BIT_OF_POWER;
        //扩容
        if (ei >= bits.length) {
            size = end;
            resize(ei + 1);
        }
        int v = val ? 0xff : 0x00;
        //设置中间的整字节
        for (int i = si + 1; i < ei; i++) {
            bits[i] = (byte) v;
        }

        //设置第si和ei字节
        int sv = (byte) (0xff << (start & (PER_BIT - 1)));
        int moveNum = (byte) (~(end & (PER_BIT - 1)) & 0x07); //最后与0000 0111相与，让前5位置0
        int ev = (byte) (0xff >>> moveNum);

        if (si == ei) {
            v = sv & ev;
            if (val) bits[si] |= v;
            else bits[si] &= ~v;
            return this;
        }
        if (val) {
            bits[si] |= sv;
            bits[ei] |= ev;
        } else {
            bits[si] &= ~sv;
            bits[ei] &= ~ev;
        }
        return this;
    }

    /**
     * 设置第几个字节上的数据为给定的值。
     * 对于int类型数据，只有最后一个字节上的数据为有效数据。
     *
     * @param nth 第几个字节，从0开始
     * @param val 设置的值
     * @return 返回this, 以便链式调用
     */
    public BitArray setNthByte(int nth, int val) {
        return setNthByte(nth, (byte) val);
    }

    /**
     * <p>
     * 设置第几个字节上的数据为给定的值，由于最高位为符号位，
     * 所以当最高比特为1时，设置的值应该是一个负数,并且保存的二制数为它的补码，
     * 也可以使用<b>setNthByte(int nth, int val)</b>重载方法，以避免负数和补码的问题
     * </p>
     * 提示：在java中<b>1000 0000</b> 对应的十进制数为 <b>(byte)-128</b>
     *
     * @param nth 第几个字节，从0开始
     * @param val 设置的值
     * @return 返回this, 以便链式调用
     */
    public BitArray setNthByte(int nth, byte val) {
        if (nth >= bits.length) {
            size = nth << PER_BIT_OF_POWER;
            resize(nth + 1);
        }
        bits[nth] = val;
        return this;
    }

    public BitArray setRangeByte(int start, int end, int val) {
        return setRangeByte(start, end, (byte) val);
    }

    public BitArray setRangeByte(int start, int end, byte val) {
        edgeCheck(start, end);
        if (end >= bits.length) {
            size = end << PER_BIT_OF_POWER;
            resize(end);
        }
        for (; start < end; start++) {
            bits[start] = val;
        }
        return this;
    }

    //扩容操作，target为目标字节数长度，默认用0填充
    private void resize(int target) {
        bits = Arrays.copyOf(bits, target);
    }

    /**
     * 获取对应比特位上的值，返回true时为1，返回false时为0
     *
     * @param pos 比特位的索引，从0开始
     * @return 对应的值，true为1，false为0
     */
    public boolean get(int pos) {
        if (pos >= size)
            throw new IndexOutOfBoundsException("获取比特位：" + pos + ",但实际只有：" + size);
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

    /**
     * 获取第几个字节上的数据
     *
     * @param nth 第几个字节，从0开始
     * @return 对应的byte数据, int类型
     */
    public int getNthByte(int nth) {
        if (nth > bits.length)
            throw new IndexOutOfBoundsException("获取字节位：" + nth + ",但实际只有 " + bits.length);
        //当最高位为1时，在byte自动转成int时，由于符号位的变化，会出现bit位数据错误
        return bits[nth] & 0xff;
    }

    public byte[] getRangeByte(int start, int end) {
        edgeCheck(start, end);
        if (end > bits.length)
            throw new IndexOutOfBoundsException();
        return Arrays.copyOfRange(bits, start, end);
    }

    /**
     * 获取二进制串所占用的字节数，结果为<b>size / 8</b>的上取整
     *
     * @return 二进制串所占用的字节数
     */
    public int getByteSize() {
        return bits.length;
    }

    /**
     * 获取实际使用的比特位长度，会根据初始化BitArray设定一个初始值，
     * 而后，当发生扩容操作时，会将该值更新
     *
     * @return 实际使用的比特位长度
     */
    public int getBitSize() {
        return size;
    }

    private void cal(BitArray bitArray, Operation operation) {
        int fByteSize = bits.length;
        int sByteSize = bitArray.getByteSize();

        for (int i = 0; i < fByteSize; i++) {
            byte f = bits[i], s = 0x00;
            if (i < sByteSize) s = (byte) bitArray.getNthByte(i);

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
     * @return 返回this, 以便链式调用
     */
    public BitArray and(BitArray bitArray) {
        cal(bitArray, (v1, v2) -> v1 & v2);
        return this;
    }

    /**
     * 将当前BitArray和给定的BitArray进行或运算，对每一个二进制位，只有都为0时结果才为0
     * 例：<pre>
     *     10001111 or
     *     01001010
     *   = 11001111</pre>
     *
     * @param bitArray 一个BitArray
     * @return 返回this, 以便链式调用
     */
    public BitArray or(BitArray bitArray) {
        cal(bitArray, ((v1, v2) -> v1 | v2));
        return this;
    }

    /**
     * 将当前BitArray进行非运算，对每一个二进制位，按位取反。
     * 例：<pre>
     *     10001111 not
     *   = 01110000</pre>
     *
     * @return 返回this, 以便链式调用
     */
    public BitArray not() {
        for (int i = 0; i < bits.length; i++) {
            bits[i] = (byte) ~bits[i];
        }
        return this;
    }

    /**
     * 将当前BitArray和给定的BitArray进行异或运算，对每一个二进制位，两个值不同时结果为1
     * 例：<pre>
     *     10001111 xor
     *     01001010
     *   = 11000101</pre>
     *
     * @param bitArray 一个BitArray
     * @return 返回this, 以便链式调用
     */
    public BitArray xor(BitArray bitArray) {
        cal(bitArray, (v1, v2) -> v1 ^ v2);
        return this;
    }

    /**
     * 将当前BitArray和给定的BitArray进行或非运算，对每一个二进制位，都为0时结果为1，等价于对或运算取反。
     * 例：<pre>
     *     10001111 nor
     *     01001010
     *   = 00110000</pre>
     *
     * @param bitArray 一个BitArray
     * @return 返回this, 以便链式调用
     */
    public BitArray nor(BitArray bitArray) {
        cal(bitArray, (v1, v2) -> ~(v1 | v2));
        return this;
    }

    /**
     * 将当前BitArray和给定的BitArray进行异或非运算，对每一个二进制位，两个值相同时结果为1，等价于对异或运算取反。
     * 例：<pre>
     *     10001111 notXor
     *     01001010
     *   = 00111010</pre>
     *
     * @param bitArray 一个BitArray
     * @return 返回this, 以便链式调用
     */
    public BitArray notXor(BitArray bitArray) {
        cal(bitArray, (v1, v2) -> ~(v1 ^ v2));
        return this;
    }

    /**
     * 将当前BitArray和给定的BitArray进行与非运算，对每一个二进制位，两个值不都为1时结果为1，等价于对与运算求反。
     * 例：<pre>
     *     10001111 notAnd
     *     01001010
     *   = 11110101</pre>
     *
     * @param bitArray 一个BitArray
     * @return 返回this, 以便链式调用
     */
    public BitArray notAnd(BitArray bitArray) {
        cal(bitArray, (v1, v2) -> ~(v1 & v2));
        return this;
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
        return builder.reverse()
                //删除开头的空格
                .delete(0, 1)
                .toString();
    }

    private void edgeCheck(int start, int end) {
        if (start >= end)
            throw new IllegalArgumentException("start:" + start + " 必须小于end:" + end);
        if (start < 0)
            throw new IllegalArgumentException("start: " + start + " 不能小于0");
    }

    @Override
    protected BitArray clone() throws CloneNotSupportedException {
        BitArray clone = (BitArray) super.clone();
        clone.bits = bits.clone();
        return clone;
    }
}
