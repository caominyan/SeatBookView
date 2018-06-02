package com.migutv.seatviewlib;

import android.graphics.RectF;

/**
 * Created by caominyan on 2018/5/31.
 */

public class Seat {

    public enum SeatStatus{
        BOOKED
        ,BOOKING
        ,UNBOOK
    }

    public enum Position{
        SEAT,//座位
        CHANNEL//走廊
    }

    private int row;

    private int column;

    private RectF mRectF = new RectF();

    private Position type = Position.SEAT;

    private SeatStatus seatBookType = SeatStatus.UNBOOK;

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public Position getType() {
        return type;
    }

    public void setType(Position type) {
        this.type = type;
    }

    public SeatStatus getSeatBookType() {
        return seatBookType;
    }

    public void setSeatBookType(SeatStatus seatBookType) {
        this.seatBookType = seatBookType;
    }

    public RectF getmRectF() {
        return mRectF;
    }

    public void setmRectF(RectF mRectF) {
        this.mRectF = mRectF;
    }
}
