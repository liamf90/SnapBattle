package com.liamfarrell.android.snapbattle.ui.createbattle;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import com.liamfarrell.android.snapbattle.R;


/**
 * Created by Liam on 6/12/2017.
 */

public class ChooseRoundsFragment extends Fragment {

    private NumberPicker mNumberOfRoundsNumberPicker;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle) {


        View view = inflater.inflate(R.layout.fragment_choose_rounds, container, false);
        mNumberOfRoundsNumberPicker = view.findViewById(R.id.numberOfRounds);
        mNumberOfRoundsNumberPicker.setMaxValue(5);
        mNumberOfRoundsNumberPicker.setMinValue(1);
        return view;
    }


  public int getRounds()
  {
      return  mNumberOfRoundsNumberPicker.getValue();
  }
}