/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blueferdi.diamondbox.arrays;

import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author ferdinand
 */
public class OddSortDemo {

    public boolean isOdd(int input) {
        return input % 2 == 1;
    }

    public boolean isOddbit(int input) {
        return (input & 1) == 1;
    }

    public void sortOdd(int[] array) {
        int i = 0, j = array.length - 1, tmp = 0;
        boolean flag = true;
        while (i != j) {
            if (flag) {
                if (isOddbit(array[i])) {
                    flag = false;
                    continue;
                } else {
                    i++;
                    continue;
                }
            } else {
                if (isOddbit(array[j])) {
                    j--;
                    continue;
                } else {
                    tmp = array[j];
                    array[j] = array[i];
                    array[i] = tmp;
                    j--;
                    flag = true;
                    continue;
                }
            }
        }
    }

    public void test(int count) {
        for (int i = 0; i < 20000; i++) {
            this.isOdd(i);
            this.isOddbit(i);
        }

        long start = 0, stop = 0;


//        start = System.nanoTime();
//        
//        for(int i=0;i<count;i++)
//        {
//            this.isOddbit(i);
//        }
//        
//        stop = System.nanoTime();
//        
//        System.out.println("bit cost : " + (stop - start));

        start = System.nanoTime();

        for (int i = 0; i < count; i++) {
            this.isOdd(i);
        }

        stop = System.nanoTime();

        System.out.println("mod cost : " + (stop - start));
    }

    public static void main(String[] args) {
        OddSortDemo osd = new OddSortDemo();
//        
//        osd.test(1000000000);
        int[][] arrays = new int[100][100000];
        Random r = new Random();
        
        for(int k=0;k<arrays.length;k++)
        {
            for (int j = 0; j < arrays[k].length; j++) {
                arrays[k][j] = r.nextInt();
            }
        }


        for (int i = 0; i < 100; i++) {

            long start = System.nanoTime();

            osd.sortOdd(arrays[i]);

            long stop = System.nanoTime();

            System.out.println(stop - start);
        }
    }
}
