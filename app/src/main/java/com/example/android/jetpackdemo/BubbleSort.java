package com.example.android.jetpackdemo;

/**
 * Created by dumingwei on 2020/5/31.
 * <p>
 * Desc:
 */
public class BubbleSort {

    private static int[] array = new int[]{1, 2, 3, 4};

    public static void main(String[] args) {
        sort(array);
    }

   /* private static void sort(int[] arr) {

        int len = arr.length;
        int temp;
        for (int i = 0; i < len - 1; i++) {
            System.out.println("i==" + i);
            boolean isOver = true;
            for (int j = 0; j < len - 1 - i; j++) {
                if (arr[j] > arr[j + 1]) {
                    temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    isOver = false;
                }
            }
            if (isOver)
                break;
        }
        for (int i : arr) {
            System.out.print(i + ",");
        }

    }*/

    public static void sort(int[] array) {
        int n = array.length;
        int temp;
        for (int i = 0; i < n - 1; i++) {
            boolean isOver = true;
            for (int j = n - 1 - i; j > i; j--) {
                if (array[j] < array[j - 1]) {
                    temp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = temp;
                    isOver = false;
                }
            }
            if (isOver)
                break;
        }
        for (int i : array) {
            System.out.print(i + ",");
        }
    }


}
