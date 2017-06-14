package com.gmugu.fallinsky;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by mugu on 16-7-4 下午3:00.
 */
public class GroundView extends View {

    private int VIEW_W;
    private int VIEW_H;

    private final String TAG = getClass().getSimpleName();

    //地面上坑的边长
    private float holeSide = 300;
    //地面颜色值
    private final int[] tilesColors = new int[]{0xffa28b54, 0xff937E4B};
    private final int tileRows = 20;
    private final int tilelines = 10;
    private Context context;
    private Bitmap mBitmap;
    private Paint eraserPaint = new Paint();

    private Paint tilesPaint = new Paint();
    private Rect tilesRect = new Rect();
    private Rect holeRect = new Rect();

    private final int tmpCount = 2;
    private Canvas[] tmpGroundCanvas = new Canvas[tmpCount];
    private Bitmap[] tmpGroundBitmap = new Bitmap[tmpCount];


    //两张位图偏移量
    public int groundOffset = 0;

    private Timer nextAnimTimer;
    private final int times = 500;//动画持续时间(毫秒)
    private final int frames = 60;//动画帧数


    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            invalidate();
        }
    }

    public Handler handler = new MyHandler();

    public GroundView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public GroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        tilesPaint.setStyle(Paint.Style.FILL);
        eraserPaint.setColor(Color.BLACK);
        eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.e(TAG, "onLayout " + changed + " " + left + " " + top + " " + right + " " + bottom);
        VIEW_W = right - left;
        VIEW_H = bottom - top;
        initTmpGroundCanvas();
    }

    private void initTmpGroundCanvas() {
        for (int i = 0; i < tmpCount; ++i) {
            if (tmpGroundBitmap[i] == null) {
                tmpGroundBitmap[i] = Bitmap.createBitmap(VIEW_W, VIEW_H, Bitmap.Config.ARGB_4444);
                tmpGroundCanvas[i] = new Canvas(tmpGroundBitmap[i]);
                resetGroupCanvas(tmpGroundCanvas[i]);
            }
        }
        resetHoleSide();
        resetGroupCanvas(tmpGroundCanvas[0]);
    }

    public void resetNextCancas() {
        resetGroupCanvas(tmpGroundCanvas[1]);
    }

    private void resetGroupCanvas(Canvas mCanvas) {
        int width = VIEW_W;
        int height = VIEW_H;

        int side = width / tileRows;
        int h = height - tilelines * side;
        tilesRect.left = -side;
        tilesRect.right = 0;
        for (int row = 0; row < tileRows; ++row) {
            tilesRect.left += side;
            tilesRect.right += side;
            tilesRect.top = h - side;
            tilesRect.bottom = h;
            for (int line = 0; line < tilelines; ++line) {
                tilesRect.top += side;
                tilesRect.bottom += side;
                tilesPaint.setColor(tilesColors[((int) (Math.random() * tilesColors.length))]);
//                Log.e(TAG, tilesRect.toString());
                mCanvas.drawRect(tilesRect, tilesPaint);
            }
        }
        holeRect.left = (int) (width / 2 - (holeSide / 2));
        holeRect.top = h;
        holeRect.right = (int) (holeRect.left + holeSide);
        holeRect.bottom = (int) (holeRect.top + holeSide);
        mCanvas.drawRect(holeRect, eraserPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (mBitmap == null) {
            mBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.mount_sheet0);
        }
        canvas.drawBitmap(mBitmap, VIEW_W / 2 - mBitmap.getWidth() / 2, VIEW_H - 4 * VIEW_W / tileRows - mBitmap.getHeight(), null);

        canvas.clipRect(0, VIEW_H - tilelines * VIEW_W / tileRows, VIEW_W, VIEW_H);
        canvas.drawBitmap(tmpGroundBitmap[0], -groundOffset, 0, null);
        canvas.drawBitmap(tmpGroundBitmap[1], VIEW_W - groundOffset, 0, null);
        canvas.clipRect(0, 0, VIEW_W, VIEW_H);
    }

    public void recycle() {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
        for (int i = 0; i < tmpGroundBitmap.length; i++) {
            Bitmap bm = tmpGroundBitmap[i];
            if (bm != null && !bm.isRecycled()) {
                bm.recycle();
                tmpGroundBitmap[i] = null;
            }
        }
    }

    public void swapGrounp() {
        Canvas tmpc = tmpGroundCanvas[0];
        tmpGroundCanvas[0] = tmpGroundCanvas[1];
        tmpGroundCanvas[1] = tmpc;
        Bitmap tmpb = tmpGroundBitmap[0];
        tmpGroundBitmap[0] = tmpGroundBitmap[1];
        tmpGroundBitmap[1] = tmpb;
    }

    @Deprecated
    public void nextLevel() {

        resetHoleSide();
        resetGroupCanvas(tmpGroundCanvas[1]);

        if (nextAnimTimer == null) {
            nextAnimTimer = new Timer();
            nextAnimTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    groundOffset += VIEW_W / (times * frames / 1000f);
                    if (groundOffset >= VIEW_W) {
                        groundOffset = 0;
                        swapGrounp();
                        cancel();
                    }
                    handler.sendEmptyMessage(0);
                }
            }, 0, 1000 / frames);
            nextAnimTimer = null;
        }

    }

    public float resetHoleSide() {
        holeSide = (float) (Math.random() * 200 + 130);
        return holeSide;
    }

    public float getHoleSide() {
        return holeSide;
    }

    public void setHoleSide(float holeSide) {
        this.holeSide = holeSide;
    }

    //获取地面高度(相对屏幕顶部)
    public int getGrounpHeight() {
        return VIEW_H - VIEW_W / tileRows * tilelines;
    }

    public int getVisibleWidth() {
        return VIEW_W;
    }


    public int getVisibleHeight() {
        return VIEW_H;
    }

    public int getGroundOffset() {
        return groundOffset;
    }

    public void setGroundOffset(int groundOffset) {
        this.groundOffset = groundOffset;
    }
}
