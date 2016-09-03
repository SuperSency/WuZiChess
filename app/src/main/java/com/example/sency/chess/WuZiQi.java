package com.example.sency.chess;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sency on 2016/8/11.
 */
public class WuZiQi extends View {

    private int mPanelWidth;
    private float mLineHeight;
    private int MAX_LINE = 10;

    private Paint mPaint = new Paint();

    private Bitmap mWhitePices;
    private Bitmap mBlackPices;

    private float ratio = 3 * 1.0f / 4;

    private ArrayList<Point> mWhiteArray = new ArrayList<>();
    private ArrayList<Point> mBlackArray = new ArrayList<>();
    //变量为true则当前轮到白棋
    private boolean isWhite = true;
    private boolean mIsGameOver;
    private boolean isWhiteWin;//true为白子赢

    final static int MAX_IN_LINE_COUNT = 5;


    public WuZiQi(Context context, AttributeSet attrs) {
        super(context, attrs);
        //  setBackgroundColor(0x44ff0000);
        init();
    }

    private void init() {
        mPaint.setColor(0x88000000);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);

        //棋子图片
        mWhitePices = BitmapFactory.decodeResource(getResources(), R.drawable.stone_w2);
        mBlackPices = BitmapFactory.decodeResource(getResources(), R.drawable.stone_b1);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = Math.min(widthSize, heightSize);
        //宽度为0
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            width = heightSize;
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            width = widthSize;
        }

        setMeasuredDimension(width, width);
    }


    //和尺寸相关
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mPanelWidth = w;
        mLineHeight = mPanelWidth * 1.0f / MAX_LINE;

        //设置棋子大小为四分之三
        int pieceWidth = (int) (mLineHeight * ratio);
        mWhitePices = Bitmap.createScaledBitmap(mWhitePices, pieceWidth, pieceWidth, false);
        mBlackPices = Bitmap.createScaledBitmap(mBlackPices, pieceWidth, pieceWidth, false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsGameOver) {
            return false;
        }
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            Point p = getValidPoint(x, y);

            if (mWhiteArray.contains(p) || mBlackArray.contains(p)) {
                return false;
            }

            if (isWhite) {
                mWhiteArray.add(p);
            } else {
                mBlackArray.add(p);
            }
            invalidate();
            isWhite = !isWhite;
            return true;
        }
        return true;
    }

    private Point getValidPoint(int x, int y) {
        return new Point((int) (x / mLineHeight), (int) (y / mLineHeight));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBoard(canvas);
        drawPieces(canvas);
        checkGame();
    }

    private void checkGame() {
        boolean whiteWin = checkFiveInLine(mWhiteArray);
        boolean blackWin = checkFiveInLine(mBlackArray);
        if (whiteWin || blackWin) {
            mIsGameOver = true;
            isWhiteWin = whiteWin;
            String text = isWhiteWin ? "白棋胜利" : "黑棋胜利";
            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("再来一局?");
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    restart();
                }
            });
            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    System.exit(0);
                }
            });
            builder.show();
        }
    }

    //清空数据
    private void restart() {
        mWhiteArray.clear();
        mBlackArray.clear();
        mIsGameOver = false;
        isWhiteWin = false;
        invalidate();
    }

    private boolean checkFiveInLine(List<Point> points) {
        for (Point p : points) {
            int x = p.x;
            int y = p.y;
            boolean win = checkHorizontal(x, y, points);
            if (win) {
                return win;
            } else if (win = checkVertical(x, y, points)) {
                return win;
            } else if (win = checkLeftDiagonal(x, y, points)) {
                return win;
            } else if (win = checkRightDiagonal(x, y, points)) {
                return win;
            }
        }
        return false;
    }

    //判断棋子是否横向有五个相邻的一致
    private boolean checkHorizontal(int x, int y, List<Point> points) {
        int count = 1;
        for (int i = 1; i < MAX_IN_LINE_COUNT; i++) {
            //横向向左
            if (points.contains(new Point(x - i, y))) {
                count++;
            } else {
                break;
            }
        }
        if (count == 5) {
            return true;
        }
        for (int i = 1; i < MAX_IN_LINE_COUNT; i++) {
            //横向向右
            if (points.contains(new Point(x + i, y))) {
                count++;
            } else {
                break;
            }
        }
        if (count == 5) {
            return true;
        }
        return false;
    }

    //判断棋子是否横向有五个相邻的一致
    private boolean checkVertical(int x, int y, List<Point> points) {
        int count = 1;
        for (int i = 1; i < MAX_IN_LINE_COUNT; i++) {
            //横向向左
            if (points.contains(new Point(x, y - i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == 5) {
            return true;
        }
        for (int i = 1; i < MAX_IN_LINE_COUNT; i++) {
            //横向向右
            if (points.contains(new Point(x, y + i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == 5) {
            return true;
        }
        return false;
    }

    //判断棋子是否横向有五个相邻的一致
    private boolean checkLeftDiagonal(int x, int y, List<Point> points) {
        int count = 1;
        for (int i = 1; i < MAX_IN_LINE_COUNT; i++) {
            //横向向左
            if (points.contains(new Point(x - i, y + i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == 5) {
            return true;
        }
        for (int i = 1; i < MAX_IN_LINE_COUNT; i++) {
            //横向向右
            if (points.contains(new Point(x + i, y - i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == 5) {
            return true;
        }
        return false;
    }

    //判断棋子是否横向有五个相邻的一致
    private boolean checkRightDiagonal(int x, int y, List<Point> points) {
        int count = 1;
        for (int i = 1; i < MAX_IN_LINE_COUNT; i++) {
            //横向向左
            if (points.contains(new Point(x + i, y + i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == 5) {
            return true;
        }
        for (int i = 1; i < MAX_IN_LINE_COUNT; i++) {
            //横向向右
            if (points.contains(new Point(x - i, y - i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == 5) {
            return true;
        }
        return false;
    }

    private void drawPieces(Canvas canvas) {
        for (int i = 0, n = mWhiteArray.size(); i < n; i++) {
            Point mWhitePoint = mWhiteArray.get(i);
            canvas.drawBitmap(mWhitePices,
                    (mWhitePoint.x + (1 - ratio) / 2) * mLineHeight,
                    (mWhitePoint.y + (1 - ratio) / 2) * mLineHeight, null);
        }
        for (int i = 0, n = mBlackArray.size(); i < n; i++) {
            Point mBlackPoint = mBlackArray.get(i);
            canvas.drawBitmap(mBlackPices,
                    (mBlackPoint.x + (1 - ratio) / 2) * mLineHeight,
                    (mBlackPoint.y + (1 - ratio) / 2) * mLineHeight, null);
        }

    }

    private void drawBoard(Canvas canvas) {
        int w = mPanelWidth;
        float lineHeight = mLineHeight;
        for (int i = 0; i < MAX_LINE; i++) {
            //起始坐标
            int startX = (int) (lineHeight / 2);
            int endX = (int) (w - lineHeight / 2);

            int y = (int) ((0.5 + i) * lineHeight);
            //横线
            canvas.drawLine(startX, y, endX, y, mPaint);
            //竖线
            canvas.drawLine(y, startX, y, endX, mPaint);
        }
    }

    //正在进行别的事情时(如打电话)，为了不让这个被销毁,即View的存储与恢复
    //默认的一些东西
    private static final String INSTANCE = "instance";
    //我们自己需要绑定的一些东西
    private static final String INSTANCE_GAME_OVER = "instance_game_over";
    private static final String INSTANCE_WHITE_ARRAY = "instance_white_array";
    private static final String INSTANCE_BLACK_ARRAY = "instance_black_array";

    //存储
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        //存一些系统默认的东西
        bundle.putParcelable(INSTANCE, super.onSaveInstanceState());
        bundle.putBoolean(INSTANCE_GAME_OVER, mIsGameOver);
        bundle.putParcelableArrayList(INSTANCE_WHITE_ARRAY, mWhiteArray);
        bundle.putParcelableArrayList(INSTANCE_BLACK_ARRAY, mBlackArray);
        return bundle;
    }

    //当重新启动Activity时，恢复
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        //state是否为Bundle类型，即是否为我们自己设置的
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mIsGameOver = bundle.getBoolean(INSTANCE_GAME_OVER);
            mWhiteArray = bundle.getParcelableArrayList(INSTANCE_WHITE_ARRAY);
            mBlackArray = bundle.getParcelableArrayList(INSTANCE_BLACK_ARRAY);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE));
            return;
        }
        super.onRestoreInstanceState(state);
    }
}
