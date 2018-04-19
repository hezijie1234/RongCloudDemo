package com.gongyou.rongclouddemo.utils;


import com.gongyou.rongclouddemo.greendaobean.Friend;

import java.util.Comparator;




/**
 *
 * @author
 *
 */
public class PinyinComparator implements Comparator<Friend> {


    public static PinyinComparator instance = null;

    public static PinyinComparator getInstance() {
        if (instance == null) {
            instance = new PinyinComparator();
        }
        return instance;
    }

    public int compare(Friend o1, Friend o2) {
        if (o1.getLetters().equals("@")
                || o2.getLetters().equals("#")) {
            //返回值为负数 o1在前o2在后，返回值为正数则o1在后o2在前，为o则不变。
            return -1;
        } else if (o1.getLetters().equals("#")
                   || o2.getLetters().equals("@")) {
            return 1;
        } else {
            return o1.getLetters().compareTo(o2.getLetters());
        }
    }

}
