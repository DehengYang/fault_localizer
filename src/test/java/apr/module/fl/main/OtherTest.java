/**
 * apr
 * Mar 4, 2022
 */
package apr.module.fl.main;

import java.util.BitSet;

import org.junit.Test;

/**
 * @author apr
 * Mar 4, 2022
 */
public class OtherTest {
    @Test
    public void bitsetTest() {
        BitSet bs1 = new BitSet(10);
        BitSet bs2 = new BitSet(10);

        bs1.set(10);
        bs1.set(20);
        bs1.set(30);
        bs1.set(40);
        bs1.set(50);

        bs2.set(60);
        bs2.set(70);
        bs2.set(50);
        bs2.set(40);
        bs2.set(30);

        System.out.println("bs1: " + bs1);
        System.out.println("bs2: " + bs2);

        bs1.and(bs2);
        System.out.println(bs1);
    }
}
