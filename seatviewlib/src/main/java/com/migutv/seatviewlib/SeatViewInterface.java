package com.migutv.seatviewlib;

import android.graphics.Canvas;

/**
 * Created by caominyan on 2018/5/31.
 */

public interface SeatViewInterface {

    void loadBaseSeatBp(int selectedDrawableId,
                        int selectingDrawableId,
                        int unselectedDrawableId);

    void drawSeats(Canvas canvas);

    void drawScreen(Canvas canvas);

    void drawRowNumber(Canvas canvas);

    void drawOverview(Canvas canvas);

}
