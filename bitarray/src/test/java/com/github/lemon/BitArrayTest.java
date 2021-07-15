package com.github.lemon;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * <p> 创建时间 2021/7/15 </p>
 *
 * @author Hami Lemon
 * @version v1.0
 */
public class BitArrayTest {

    @Test
    public void testNot() {
        BitArray bitArray = new BitArray(8);
        bitArray.setNthByte(0, 0xf1)
                .not();
        Assertions.assertThat(bitArray.toString())
                .isEqualTo("00001110");
    }

}