package com.github.lemon;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * <p> 创建时间 2021/7/15 </p>
 *
 * @author Hami Lemon
 * @version v1.0
 */
public class BitArrayTest {

    @Test
    public void test(){
        BitArray bitArray = new BitArray();
        bitArray.active(0);
        System.out.println(bitArray);
    }

}