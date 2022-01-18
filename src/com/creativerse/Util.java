package com.creativerse;

public class Util {
    public static int pair(int x, int y) {
        double p = 0.5 * (x+y) * (x+y+1) + y;

        return (int) Math.round(p);
    }

    public static int[] unpair(int p) {
        int w = (int) Math.floor((Math.sqrt(8*p + 1) - 1) / 2);
        int t = (int) (0.5 * (Math.pow(w, 2) + w));
        int y = p - t;
        int x = w - y;

        return new int[] {x,y};
    }


}
