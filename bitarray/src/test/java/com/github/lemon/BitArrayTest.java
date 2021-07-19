package com.github.lemon;

import static org.assertj.core.api.Assertions.*;

import org.assertj.core.data.Index;
import org.junit.Test;

/**
 * <p> 创建时间 2021/7/15 </p>
 *
 * @author Hami Lemon
 * @version v1.0
 */
public class BitArrayTest {
    //构造函数测试
    @Test
    public void constructTest() {
        BitArray bit = new BitArray();
        assertThat(bit.toString())
                .as("无参构造")
                .isEqualTo("00000000 00000000 00000000 00000000");

        bit = new BitArray(0);
        assertThat(bit.toString())
                .as("0个比特位构造")
                .isEmpty();
        bit = new BitArray(1);
        assertThat(bit.toString())
                .as("1个比特位构造")
                .isEqualTo("00000000");
        bit = new BitArray(8);
        assertThat(bit.toString())
                .as("8个比特位构造")
                .isEqualTo("00000000");
        bit = new BitArray(9);
        assertThat(bit.toString())
                .as("9个比特位构造")
                .isEqualTo("00000000 00000000");

        bit = new BitArray(new byte[]{1, 2});
        assertThat(bit.toString())
                .as("传入byte数组构造")
                .isEqualTo("00000010 00000001");

        bit = new BitArray("a");
        assertThat(bit.toString())
                .as("按字符串a构造")
                .isEqualTo("01100001");
    }

    @Test
    public void valueOfTest() {
        BitArray bit = BitArray.valueOf((byte) 1);
        assertThat(bit.toString())
                .as("静态 byte=1 创建")
                .isEqualTo("00000001");

        bit = BitArray.valueOf((short) 257);
        assertThat(bit.toString())
                .as("静态 short=257 创建")
                .isEqualTo("00000001 00000001");

        bit = BitArray.valueOf(16843009);
        assertThat(bit.toString())
                .as("静态 int=16843009 创建")
                .isEqualTo("00000001 00000001 00000001 00000001");

        bit = BitArray.valueOf(72057594037927937L);
        assertThat(bit.toString())
                .as("静态 long=72057594037927937 创建")
                .isEqualTo("00000001 00000000 00000000 00000000 00000000 00000000 00000000 00000001");

        bit = BitArray.valueOf("111000 10100101");
        assertThat(bit.toString())
                .as("静态 string=111000 10100101 创建")
                .isEqualTo("00111000 10100101");

        bit = BitArray.valueOf("0011100010100101");
        assertThat(bit.toString())
                .as("静态 string=11100010100101 创建")
                .isEqualTo("00111000 10100101");

        bit = BitArray.valueOf("");
        assertThat(bit.toString())
                .as("静态 string= 创建")
                .isEqualTo("");

        bit = BitArray.valueOf("1");
        assertThat(bit.toString())
                .as("静态 string=1 创建")
                .isEqualTo("00000001");
    }

    @Test
    public void setTest() {
        BitArray bit = new BitArray();
        bit.active(0);
        assertThat(bit.toString())
                .as("0位置1")
                .isEqualTo("00000000 00000000 00000000 00000001");
        bit.passive(0);
        assertThat(bit.toString())
                .as("0位置0")
                .isEqualTo("00000000 00000000 00000000 00000000");

        bit.active(0, 1);
        assertThat(bit.toString())
                .as("0字节 1 位置1")
                .isEqualTo("00000000 00000000 00000000 00000010");
        bit.passive(0, 1);
        assertThat(bit.toString())
                .as("0字节 1 位置0")
                .isEqualTo("00000000 00000000 00000000 00000000");

        bit.active(3, 7);
        assertThat(bit.toString())
                .as("3字节 7 位置1")
                .isEqualTo("10000000 00000000 00000000 00000000");
        bit.passive(3, 7);
        assertThat(bit.toString())
                .as("3字节 7 位置0")
                .isEqualTo("00000000 00000000 00000000 00000000");

        bit.active(32);
        assertThat(bit.toString())
                .as("32位置1")
                .isEqualTo("00000001 00000000 00000000 00000000 00000000");
        bit.passive(32);
        assertThat(bit.toString())
                .as("32位置1")
                .isEqualTo("00000000 00000000 00000000 00000000 00000000");
    }

    @Test
    public void setRangeTest() {
        assertThatThrownBy(() -> {
            BitArray bit = new BitArray();
            bit.activeRange(-1, 0);
        }).as("置1范围 -1,0")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("start: -1 不能小于0");

        assertThatThrownBy(() -> {
            BitArray bit = new BitArray();
            bit.activeRange(0, 0);
        }).as("置1范围 0,0")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("start:0 必须小于end:0");

        BitArray bit = new BitArray();
        bit.activeRange(0, 8);
        assertThat(bit.toString())
                .as("置1 0,8")
                .isEqualTo("00000000 00000000 00000000 11111111");

        bit.passiveRange(0, 8);
        assertThat(bit.toString())
                .as("置0 0,8")
                .isEqualTo("00000000 00000000 00000000 00000000");

        bit.activeRange(1, 7);
        assertThat(bit.toString())
                .as("置1 1,7")
                .isEqualTo("00000000 00000000 00000000 01111110");

        bit.passiveRange(1, 7);
        assertThat(bit.toString())
                .as("置0 1,7")
                .isEqualTo("00000000 00000000 00000000 00000000");

        bit.activeRange(1, 15);
        assertThat(bit.toString())
                .as("置1 1,15")
                .isEqualTo("00000000 00000000 01111111 11111110");

        bit.passiveRange(1, 15);
        assertThat(bit.toString())
                .as("置0 1,15")
                .isEqualTo("00000000 00000000 00000000 00000000");

        bit.activeRange(0, 32);
        assertThat(bit.toString())
                .as("置1 0,32")
                .isEqualTo("11111111 11111111 11111111 11111111");

        bit.passiveRange(0, 32);
        assertThat(bit.toString())
                .as("置0 0,32")
                .isEqualTo("00000000 00000000 00000000 00000000");

        bit.activeRange(32, 40);
        assertThat(bit.toString())
                .as("置1 32,40")
                .isEqualTo("11111111 00000000 00000000 00000000 00000000");

        bit.passiveRange(32, 40);
        assertThat(bit.toString())
                .as("置0 32,40")
                .isEqualTo("00000000 00000000 00000000 00000000 00000000");

        bit = new BitArray();
        bit.activeRange(32, 48);
        assertThat(bit.toString())
                .as("置1 32,48")
                .isEqualTo("11111111 11111111 00000000 00000000 00000000 00000000");

        bit.passiveRange(32, 48);
        assertThat(bit.toString())
                .as("置0 32,48")
                .isEqualTo("00000000 00000000 00000000 00000000 00000000 00000000");
    }

    @Test
    public void setByteTest() {
        BitArray bit = new BitArray(8);
        bit.setNthByte(0, 0xff);
        assertThat(bit.toString())
                .as("0字节置11111111")
                .isEqualTo("11111111");

        bit.setNthByte(0, (byte) -128);
        assertThat(bit.toString())
                .as("0字节置10000000")
                .isEqualTo("10000000");

        bit = new BitArray(16);
        bit.setNthByte(1, 0xff);
        assertThat(bit.toString())
                .as("1字节置11111111")
                .isEqualTo("11111111 00000000");

        bit = new BitArray(16);
        bit.setRangeByte(0, 2, 0xff);
        assertThat(bit.toString())
                .as("[0,2)字节置11111111")
                .isEqualTo("11111111 11111111");

        bit.setRangeByte(0, 2, (byte) -128);
        assertThat(bit.toString())
                .as("[0,2)字节置10000000")
                .isEqualTo("10000000 10000000");

        bit = new BitArray(16);
        bit.setRangeByte(3, 5, 0xff);
        assertThat(bit.toString())
                .as("[3,5)字节置11111111")
                .isEqualTo("11111111 11111111 00000000 00000000 00000000");
    }

    @Test
    public void getTest() {
        BitArray bit = BitArray.valueOf("00000010");
        final boolean v1 = bit.get(0);
        assertThat(v1)
                .as("00000010获取第0位")
                .isEqualTo(false);

        final boolean v2 = bit.get(1);
        assertThat(v2)
                .as("00000010获取第1位")
                .isEqualTo(true);

        assertThatThrownBy(() -> bit.get(8))
                .as("越界获取00000010第8位")
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessage("获取比特位：8,但实际只有：8");
    }

    @Test
    public void getByteTest() {
        //BUG#1 最高位为1时，如果用int接收，会出现错误
        BitArray bit = BitArray.valueOf("11110000 00001111");
        final int v1 = bit.getNthByte(0);
        assertThat(v1)
                .as("11110000 00001111获取第0字节")
                .isEqualTo(0x0f);

        final int v2 = bit.getNthByte(1);
        assertThat(v2)
                .as("11110000 00001111获取第1字节")
                .isEqualTo(0xf0);

        assertThatThrownBy(() -> bit.getNthByte(2))
                .as("越界获取11110000 00001111第2字节")
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    public void getRangeByteTest() {
        BitArray bit = BitArray.valueOf("11111111 00000000 11110000 00001111");

        final byte[] v1 = bit.getRangeByte(0, 1);
        assertThat(v1)
                .as("11111111 00000000 11110000 00001111获取[0,1)字节")
                .isNotEmpty()
                .hasSize(1)
                .contains(0x0f, atIndex(0));

        final byte[] v2 = bit.getRangeByte(0, 4);
        assertThat(v2)
                .as("11111111 00000000 11110000 00001111获取[0,4)字节")
                .isNotEmpty()
                .hasSize(4)
                .contains(0x0f, atIndex(0))
                .contains(0xf0, atIndex(1))
                .contains(0x00, atIndex(2))
                .contains(0xff, atIndex(3));

        final byte[] v3 = bit.getRangeByte(1, 4);
        assertThat(v3)
                .as("11111111 00000000 11110000 00001111获取[1,4)字节")
                .isNotEmpty()
                .hasSize(3)
                .contains(0xf0, atIndex(0))
                .contains(0x00, atIndex(1))
                .contains(0xff, atIndex(2));

        assertThatThrownBy(() -> bit.getRangeByte(0, 0))
                .as("越界获取[0,0)")
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> bit.getRangeByte(-1, 0))
                .as("越界获取[-1,0)")
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> bit.getRangeByte(0, 5))
                .as("越界获取[0,5)")
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    public void getByteSizeTest() {
        BitArray bit = new BitArray();
        final int v1 = bit.getByteSize();
        assertThat(v1)
                .as("获取字节数")
                .isEqualTo(4);

        bit = new BitArray(0);
        final int v2 = bit.getByteSize();
        assertThat(v2)
                .as("获取字节数")
                .isEqualTo(0);

        bit.active(1, 1);
        final int v3 = bit.getByteSize();
        assertThat(v3)
                .as("扩容后获取字节数")
                .isEqualTo(2);
    }

    @Test
    public void getBitSizeTest() {
        BitArray bit = new BitArray();
        final int v1 = bit.getBitSize();
        assertThat(v1)
                .as("获取比特位数")
                .isEqualTo(32);

        bit = new BitArray(0);
        final int v2 = bit.getBitSize();
        assertThat(v2)
                .as("获取比特位数")
                .isEqualTo(0);

        bit.active(1, 2);
        final int v3 = bit.getBitSize();

        assertThat(v3)
                .as("扩容后获取比特位数")
                .isEqualTo(10);
    }

    @Test
    public void logicTest() throws CloneNotSupportedException {
        BitArray bit1 = BitArray.valueOf("10100101 11110000");
        BitArray bit2 = BitArray.valueOf("10101010 00001111");

        BitArray bit3 = new BitArray(8);
        bit3.setNthByte(0, 0xf0);
        bit3.not();
        assertThat(bit3.toString())
                .as("11110000非运算")
                .isEqualTo("00001111");

        BitArray bit = bit1.clone();
        bit.and(bit2);
        assertThat(bit.toString())
                .as("与运算")
                .isEqualTo("10100000 00000000");

        bit = bit1.clone();
        bit.or(bit2);
        assertThat(bit.toString())
                .as("或运算")
                .isEqualTo("10101111 11111111");

        bit = bit1.clone();
        bit.xor(bit2);
        assertThat(bit.toString())
                .as("异或运算")
                .isEqualTo("00001111 11111111");

        bit = bit1.clone();
        bit.nor(bit2);
        assertThat(bit.toString())
                .as("或非运算")
                .isEqualTo("01010000 00000000");

        bit = bit1.clone();
        bit.notXor(bit2);
        assertThat(bit.toString())
                .as("异或非（同或）运算")
                .isEqualTo("11110000 00000000");

        bit = bit1.clone();
        bit.notAnd(bit2);
        assertThat(bit.toString())
                .as("与非运算")
                .isEqualTo("01011111 11111111");
    }

    @Test
    public void test(){
        System.out.println(1<<3);
    }
}