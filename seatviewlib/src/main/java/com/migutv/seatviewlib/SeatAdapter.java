package com.migutv.seatviewlib;

/**
 * Created by caominyan on 2018/5/31.
 */

public class SeatAdapter {

    /**
     * 座位一定是长方形，如果不规则，可以将座位的type 设置为channel
     */
    private Seat[][] mSeats;

    private int row_num;

    private int column_num;


    private SeatAdapter(Seat[][] seats){
        this.mSeats = seats;
        row_num = seats.length;
        column_num = seats[0].length;
    }

    public static SeatAdapter creatAdapter(Seat[][] seats){
        if(seats == null || seats[0] ==null){
            return null;
        }else{
            return new SeatAdapter(seats);
        }
    }

    /**
     * 获取某个座位的信息
     * @param row
     * @param column
     * @return
     */
    public Seat getSeatInfo(int row,int column){
        try{
            return mSeats[row][column];
        }catch (IndexOutOfBoundsException ex){
            return null;
        }
    }

    public int getRow_num() {
        return row_num;
    }

    public int getColumn_num() {
        return column_num;
    }

}
