package com.migutv.seatviewlib;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.migutv.seatviewlib.bean.Seat;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 用户自己自定义的座位图片必须一样
 * Created by caominyan on 2018/5/31.
 */

public class SeatView extends View implements SeatViewInterface {

    private String Tag = "SeatView";

    private SeatAdapter mSeatAdapter;//数据集合

    private Bitmap selectedDrawable;//已被选择的座位图
    private Bitmap selectingDrawable;//用户正在选择的座位图
    private Bitmap unselectedDrawable;//未被选择的座位图

    private int seatBpWidth;
    private int seatBpHeight;

    private int seatsWidth;
    private int seatsHeight;

    private int rowSpace;
    private int columnSpace;

    private int screenWidth;
    private int screenHeight;

    private int leftTextpadding;
    private int leftPadding;
    private int rightPadding;
    private int bottomPadding;
    private int seatToScreen;
    private int rowTextWidth;

    private String screenTxt;

    private Paint screenPait;//绘画大屏幕的画笔
    private Paint screenTextPait;
    private Paint rowNumPaint;
    private Paint rowNumBgPaint;
    private Matrix matrix;
    private float scale = 1;
    private float moveX = 0;
    private float moveY = 0;

    public SeatView(Context context) {
        super(context);
        init(context, null);
    }

    public SeatView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SeatView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Custom_SeatView, 0, 0);
        int selectedDrawableId = typedArray.getResourceId(R.styleable.Custom_SeatView_selected_seat_drawable, R.drawable.seat_sold);
        int selectingDrawableId = typedArray.getResourceId(R.styleable.Custom_SeatView_selecting_seat_drawable, R.drawable.seat_green);
        int unselectedDrawableId = typedArray.getResourceId(R.styleable.Custom_SeatView_unselected_seat_drawable, R.drawable.seat_gray);
        rowSpace = typedArray.getDimensionPixelOffset(R.styleable.Custom_SeatView_row_space, (int) Util.dip2Px(context, 10));
        columnSpace = typedArray.getDimensionPixelOffset(R.styleable.Custom_SeatView_column_space, (int) Util.dip2Px(context, 3));
        screenWidth = (int) Util.dip2Px(context, 150);
        screenHeight = (int) Util.dip2Px(context, 25);
        leftPadding = (int) Util.dip2Px(context, 45);
        rightPadding = (int) Util.dip2Px(context, 30);
        bottomPadding = (int) Util.dip2Px(context, 20);
        seatToScreen = (int) Util.dip2Px(context, 20);
        leftTextpadding = (int) Util.dip2Px(context, 15);
        rowTextWidth = (int) Util.dip2Px(context, 20);
        typedArray.recycle();
        loadBaseSeatBp(selectedDrawableId, selectingDrawableId, unselectedDrawableId);
        initPaint();
        initOtherRs();
    }

    /**
     * 设置参数
     *
     * @param seatAdapter
     */
    public void setAdapter(SeatAdapter seatAdapter) {
        this.mSeatAdapter = seatAdapter;
        initSeatWidth();
        initSeatHeight();
        postInvalidate();
    }

    private void initOtherRs() {
        matrix = new Matrix();
        screenTxt = getResources().getString(R.string.screen_text);
    }

    private void initPaint() {
        screenPait = new Paint();
        screenPait.setColor(Color.parseColor("#000000"));
        screenTextPait = new Paint();
        screenTextPait.setColor(Color.parseColor("#ffffff"));
        screenTextPait.setTextSize(Util.sp2px(getContext(), 10));
        rowNumPaint = new Paint();
        rowNumPaint.setColor(Color.parseColor("#000000"));
        rowNumPaint.setTextSize(Util.sp2px(getContext(), 12));
        rowNumBgPaint = new Paint();
        rowNumBgPaint.setColor(Color.parseColor("#3e00ff00"));
        rowNumBgPaint.setAntiAlias(true);
    }

    private void initSeatWidth() {
        seatsWidth = (mSeatAdapter.getColumn_num() - 1) * (columnSpace)
                + mSeatAdapter.getColumn_num() * seatBpWidth;
        seatsWidth = (int) (seatsWidth * scale);

    }

    private void initSeatHeight() {
        seatsHeight = (mSeatAdapter.getRow_num() - 1) * (rowSpace)
                + mSeatAdapter.getRow_num() * seatBpHeight;

        seatsHeight = (int) (seatsHeight * scale);
    }

    @Override
    public void loadBaseSeatBp(int selectedDrawableId,
                               int selectingDrawableId,
                               int unselectedDrawableId) {
        selectedDrawable = BitmapFactory.decodeResource(getResources(), selectedDrawableId);
        selectingDrawable = BitmapFactory.decodeResource(getResources(), selectingDrawableId);
        unselectedDrawable = BitmapFactory.decodeResource(getResources(), unselectedDrawableId);
        seatBpWidth = selectedDrawable.getWidth();
        seatBpHeight = selectedDrawable.getHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawSeats(canvas);
        drawScreen(canvas);
        drawRowNumber(canvas);
    }

    @Override
    public void drawSeats(Canvas canvas) {
        int rowSize = mSeatAdapter.getRow_num();//行数
        int columnSize = mSeatAdapter.getColumn_num();//列数
        int rowPosition = (int) (screenHeight + seatToScreen + moveY);
        for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
            int columnPosition = (int) (leftPadding + moveX);
            for (int columnIndex = 0; columnIndex < columnSize; columnIndex++) {
                if (mSeatAdapter.getSeatInfo(rowIndex, columnIndex).getType() == Seat.Position.CHANNEL) {
                    //是走廊
                    columnPosition += (seatBpWidth * scale + columnSpace * scale);
                } else {
                    Seat seat = mSeatAdapter.getSeatInfo(rowIndex, columnIndex);
                    matrix.setTranslate(columnPosition, rowPosition);
                    matrix.postScale(scale, scale, columnPosition, rowPosition);
                    if (seat.getSeatBookType() == Seat.SeatStatus.UNBOOK) {
                        //未被选择
                        canvas.drawBitmap(unselectedDrawable, matrix, null);
                    } else if (seat.getSeatBookType() == Seat.SeatStatus.BOOKED) {
                        //已被选择
                        canvas.drawBitmap(selectedDrawable, matrix, null);
                    } else {
                        //正在被选择的
                        canvas.drawBitmap(selectingDrawable, matrix, null);
                    }
                    seat.getmRectF().set(0, 0, seatBpWidth, seatBpHeight);
                    matrix.mapRect(seat.getmRectF());
                    columnPosition += (seatBpWidth * scale + columnSpace * scale);
                }
            }
            rowPosition += (seatBpHeight * scale + rowSpace * scale);
        }
    }

    @Override
    public void drawScreen(Canvas canvas) {
        initSeatWidth();
        initSeatHeight();

        int centerX = (int) (seatsWidth / 2 + leftPadding + moveX);
        int startY = 0;
        Path path = new Path();
        path.moveTo(centerX, startY);
        path.lineTo(centerX - screenWidth / 2, startY);
        path.lineTo(centerX - screenWidth / 2 + 20, screenHeight);
        path.lineTo(centerX + screenWidth / 2 - 20, screenHeight);
        path.lineTo(centerX + screenWidth / 2, startY);
        canvas.drawPath(path, screenPait);

        Rect bounds = new Rect();
        screenTextPait.getTextBounds(screenTxt, 0, screenTxt.length(), bounds);

        canvas.drawText(screenTxt,
                centerX - bounds.width() / 2,
                getBaseLine(screenTextPait, startY, startY + screenHeight),
                screenTextPait);

    }

    @Override
    public void drawRowNumber(Canvas canvas) {

        int rowSize = mSeatAdapter.getRow_num();//行数
        int rowPosition = (int) (screenHeight + seatToScreen + moveY);
        int startRowPosition = rowPosition;
        RectF rectF = new RectF();

        for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
            rowPosition += (seatBpHeight * scale + rowSpace * scale);
        }
        rectF.set(leftTextpadding, startRowPosition - rowSpace * scale,
                leftTextpadding + rowTextWidth, rowPosition);
        canvas.drawRect(rectF, rowNumBgPaint);


        rowPosition = (int) (screenHeight + seatToScreen + moveY);
        for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
            canvas.drawText(String.valueOf(rowIndex),
                    leftTextpadding / 2 +(leftTextpadding + rowTextWidth - rowNumPaint.measureText(String.valueOf(rowIndex))) / 2,
                    getBaseLine(rowNumPaint, rowPosition, rowPosition + seatBpHeight * scale),
                    rowNumPaint);
            rowPosition += (seatBpHeight * scale + rowSpace * scale);

        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        mScaleGestureDetector.onTouchEvent(event);

        boolean mutiPointer = false;
        int pointerCount = event.getPointerCount();
        if (pointerCount > 1) {
            mutiPointer = true;
        }


        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (!mutiPointer) {
                    int visiableHeight = getMeasuredHeight();
                    int visiableWidth = getMeasuredWidth();
                    Seat lefttop = mSeatAdapter.getSeatInfo(0, 0);
                    Seat rightBottom = mSeatAdapter.getSeatInfo(mSeatAdapter.getRow_num() - 1
                            , mSeatAdapter.getColumn_num() - 1);
                    float top = lefttop.getmRectF().top;
                    float left = lefttop.getmRectF().left;
                    float right = rightBottom.getmRectF().right;
                    float bottom = rightBottom.getmRectF().bottom;

                    float moveToY = -1;
                    float moveToX = -1;
                    if (seatsHeight > visiableHeight) {
                        if (top < 0) {
                            if (bottom < 0) {
                                moveToY = visiableHeight - seatsHeight - screenHeight - seatToScreen - rightPadding;
                            } else if (bottom >= 0 && bottom <= visiableHeight) {
                                moveToY = visiableHeight - seatsHeight - screenHeight - seatToScreen - rightPadding;
                            } else {
                                //不需要移动
                            }
                        } else if (top >= 0 && top <= visiableHeight) {
                            if (bottom >= 0 && bottom <= visiableHeight) {
                                //不需要移动
                            } else {
                                moveToY = 0f;
                            }
                        } else {
                            moveToY = 0f;
                        }
                    } else {
                        if (top < 0) {
                            if (bottom < 0) {
                                moveToY = 0f;
                            } else if (bottom >= 0 && bottom <= visiableHeight) {
                                moveToY = 0f;
                            } else {
                                //不需要移动
                            }
                        } else if (top >= 0 && top <= visiableHeight) {
                            if (bottom >= 0 && bottom <= visiableHeight) {
                                //不需要移动
                            } else {
                                moveToY = 0f;
                            }
                        } else {
                            moveToY = 0f;
                        }
                    }

                    if (seatsWidth > visiableWidth) {
                        //座位表比屏幕小
                        if (left < 0) {
                            if (right < 0) {
                                moveToX = visiableWidth - seatsWidth - rightPadding - leftPadding;
                            } else if (right >= 0 && right <= visiableWidth) {
                                moveToX = visiableWidth - seatsWidth - rightPadding - leftPadding;
                            } else {
                                //不需要移动
                            }
                        } else if (left >= 0 && left <= visiableWidth) {
                            if (right >= 0 && right <= visiableWidth) {
                                //不需要移动
                            } else {
                                moveToX = 0f;
                            }
                        } else {
                            moveToX = 0f;
                        }
                    } else {
                        //座位表比屏幕小
                        if (left < 0) {
                            if (right < 0) {
                                moveToX = 0f;
                            } else if (right >= 0 && right <= visiableWidth) {
                                moveToX = 0f;
                            } else {
                                //不需要移动
                            }
                        } else if (left >= 0 && left <= visiableWidth) {
                            if (right >= 0 && right <= visiableWidth) {
                                //不需要移动
                            } else {
                                moveToX = 0f;
                            }
                        } else {
                            moveToX = 0f;
                        }
                    }


                    autoMove(moveToX, moveToY);


                }

                break;
        }

        return true;
    }

    private void autoMove(float toMoveX, float toMoveY) {
        final float moveStartX = moveX;
        final float moveStartY = moveY;
        ValueAnimator valueAnimatorX = null;
        if (toMoveX != -1) {
            valueAnimatorX = ValueAnimator.ofFloat(moveStartX, toMoveX);
            valueAnimatorX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    moveX = (float) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
        }
        ValueAnimator valueAnimatorY = null;
        if (toMoveY != -1) {
            valueAnimatorY = ValueAnimator.ofFloat(moveStartY, toMoveY);
            valueAnimatorY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    moveY = (float) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
        }

        Collection<Animator> animatorList = new ArrayList<>(4);
        if (valueAnimatorX != null) {
            animatorList.add(valueAnimatorX);
        }
        if (valueAnimatorY != null) {
            animatorList.add(valueAnimatorY);
        }
        if (animatorList.size() > 0) {
            AnimatorSet animationSet = new AnimatorSet();
            animationSet.playTogether(animatorList);
            animationSet.setDuration(200);
            animationSet.start();
        }
    }


    private void autoScale(float toScale) {
        final float initStart = scale;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(initStart, toScale);
        valueAnimator.setDuration(200);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                scale = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        valueAnimator.start();
    }

    private float getBaseLine(Paint p, float top, float bottom) {
        Paint.FontMetrics fontMetrics = p.getFontMetrics();
        int baseline = (int) ((bottom + top - fontMetrics.bottom - fontMetrics.top) / 2);
        return baseline;
    }

    private GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.e(Tag, "distanceX:" + distanceX);
            moveX -= distanceX;
            moveY -= distanceY;
            Log.e(Tag, "distanceY:" + distanceY);
            postInvalidate();
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.e(Tag, "onSingleTapConfirmed");
            float x = e.getX();
            float y = e.getY();
            Seat seat = getClickItemToSeat(x, y);
            if (seat != null && seat.getSeatBookType() != Seat.SeatStatus.BOOKED) {
                if (seat.getSeatBookType() == Seat.SeatStatus.BOOKING) {
                    seat.setSeatBookType(Seat.SeatStatus.UNBOOK);
                } else {
                    seat.setSeatBookType(Seat.SeatStatus.BOOKING);
                }
            }

            postInvalidate();
            return super.onSingleTapConfirmed(e);
        }
    });

    private Seat getClickItemToSeat(float x, float y) {
        Log.e(Tag, "i=" + x + " j=" + y);
        outer:
        for (int i = 0; i < mSeatAdapter.getRow_num(); i++) {

            Seat rowSeat = mSeatAdapter.getSeatInfo(i, 0);
            if (rowSeat.getmRectF().top < y && rowSeat.getmRectF().bottom > y) {
                for (int j = 0; j < mSeatAdapter.getColumn_num(); j++) {
                    Seat seat = mSeatAdapter.getSeatInfo(i, j);
                    if (seat.getmRectF().contains(x, y)) {
                        return seat;
                    }
                }
                return null;
            }

        }
        return null;
    }

    private ScaleGestureDetector mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.OnScaleGestureListener() {

        float lastScale;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            Log.e(Tag, "scaleFactor:" + scaleFactor);
            if (lastScale * scaleFactor > 2.5f) {
                scale = 2.5f;
            } else if (lastScale * scaleFactor < 0.8) {
                lastScale = 0.8f;
            } else {
                scale = lastScale * scaleFactor;
            }

            postInvalidate();
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            lastScale = scale;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            //缩放后的处理
            if (scale < 1f || scale > 2f) {
                if (Math.abs(scale - 1) < Math.abs(scale - 2)) {
                    autoScale(1f);
                } else {
                    autoScale(2f);
                }
            }

        }
    });
}
