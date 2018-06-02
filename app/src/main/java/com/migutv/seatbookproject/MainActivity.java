package com.migutv.seatbookproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.migutv.seatviewlib.Seat;
import com.migutv.seatviewlib.SeatAdapter;
import com.migutv.seatviewlib.SeatView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SeatView seatview = findViewById(R.id.seatview);

        Seat[][] mSeats = new Seat[15][15];
        for(int index = 0 ; index < 15 ; index ++){
            for(int jndex = 0 ; jndex < 15 ; jndex ++){
                Seat seat = new Seat();
                mSeats[index][jndex] = seat;
            }
        }


        seatview.setAdapter(SeatAdapter.creatAdapter(mSeats));

    }
}
