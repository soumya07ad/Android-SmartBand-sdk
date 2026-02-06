package com.sxr.sdk.ble.keepfit.client;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;

public class ImageUtil {

    public static Bitmap createBitmap2(View v) {
        Bitmap bmp = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.WHITE);
        v.draw(c);
        return bmp;
    }

    public static Bitmap takeScreenShot(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();

        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay().getHeight();
        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight);
        view.destroyDrawingCache();
        return b;
    }

    public static void saveBitmap(Bitmap b, String strFileName) {
        File f = new File("", strFileName);
        if (f.exists())
            f.delete();

        try {
            FileOutputStream out = new FileOutputStream(f);
            b.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        }  catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static Bitmap combineBitmap(Bitmap background, Bitmap foreground, float height) {
        Paint p = new Paint();
        int bgWidth = background.getWidth();
        int bgHeight = background.getHeight();
        //create the new blank bitmap 创建一个新的和SRC长度宽度一样的位图
        Bitmap newbmp = Bitmap.createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(newbmp);
        //draw bg into
        cv.drawBitmap(background, 0, 0, p);//在 0，0坐标开始画入bg
        //draw fg into
//		p.setAlpha(120);
        cv.drawBitmap(foreground, 0, height, p);//在 0，0坐标开始画入fg ，可以从任意位置画入
        //save all clip
//		cv.save(Canvas.ALL_SAVE_FLAG);
        cv.save();
        //store
        cv.restore();//存储
        return newbmp;
    }

}