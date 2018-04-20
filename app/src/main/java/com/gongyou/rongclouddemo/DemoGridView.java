package com.gongyou.rongclouddemo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

/**
 * Created by Bob on 15/7/31.
 */
public class DemoGridView extends GridView {

    public DemoGridView(Context context) {
        super(context);
    }

    public DemoGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DemoGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    /**
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     * 在自定义View和ViewGroup的时候，我们经常会遇到int型的MeasureSpec来表示一个组件的大小，这个变量里面不仅有组件的尺寸大小，还有大小的模式。
    这个大小的模式，有点难以理解。在系统中组件的大小模式有三种：
    1.精确模式（MeasureSpec.EXACTLY）
    在这种模式下，尺寸的值是多少，那么这个组件的长或宽就是多少。
    2.最大模式（MeasureSpec.AT_MOST）
    这个也就是父组件，能够给出的最大的空间，当前组件的长或宽最大只能为这么大，当然也可以比这个小。
    3.未指定模式（MeasureSpec.UNSPECIFIED）
    这个就是说，当前组件，可以随便用空间，不受限制。
    可能有很多人想不通，一个int型整数怎么可以表示两个东西（大小模式和大小的值），一个int类型我们知道有32位。而模式有三种，要表示三种状 态，至少得2位二进制位。于是系统采用了最高的2位表示模式。如图：

    最高两位是00的时候表示”未指定模式”。即MeasureSpec.UNSPECIFIED
    最高两位是01的时候表示”’精确模式”。即MeasureSpec.EXACTLY
    最高两位是11的时候表示”最大模式”。即MeasureSpec.AT_MOST
    很多人一遇到位操作头就大了，为了操作简便，于是系统给我提供了一个MeasureSpec工具类。
    这个工具类有四个方法和三个常量（上面所示）供我们使用：

    //这个是由我们给出的尺寸大小和模式生成一个包含这两个信息的int变量，这里这个模式这个参数，传三个常量中的一个。
    public static int makeMeasureSpec(int size, int mode)

    //这个是得到这个变量中表示的模式信息，将得到的值与三个常量进行比较。
    public static int getMode(int measureSpec)

    //这个是得到这个变量中表示的尺寸大小的值。
    public static int getSize(int measureSpec)

    //把这个变量里面的模式和大小组成字符串返回来，方便打日志
    public static String toString(int measureSpec)

    MeasureSpec.EXACTLY：当我们将控件的layout_width或layout_height指定为具体数值时如andorid:layout_width=”50dip”，或者为FILL_PARENT是，都是控件大小已经确定的情况，都是精确尺寸。
    MeasureSpec.AT_MOST是最大尺寸，当控件的layout_width或layout_height指定为WRAP_CONTENT时，控件大小一般随着控件的子空间或内容进行变化，此时控件尺寸只要不超过父控件允许的最大尺寸即可。因此，此时的mode是AT_MOST，size给出了父控件允许的最大尺寸。
    MeasureSpec.UNSPECIFIED是未指定尺寸，这种情况不多，一般都是父控件是AdapterView，通过measure方法传入的模式
     */
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }


}
