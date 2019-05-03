package com.liamfarrell.android.snapbattle.activity.createbattle;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.liamfarrell.android.snapbattle.app.App;
import com.liamfarrell.android.snapbattle.R;

import java.util.ArrayList;

/**
 * Created by liamf on 28/02/2018.
 */

public class ChooseVotingFragment extends Fragment implements View.OnClickListener  {

    private Spinner mVotingLengthSpinner;
    private VotingChoice mChosenVotingChoice;



    @Override
    public void onClick(View view) {
        // determine which voting option is checked and update the member variables
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.noVotingButton:
                if (checked) {
                    mVotingLengthSpinner.setEnabled(false);
                    mChosenVotingChoice = VotingChoice.NONE;
                }
                break;
            case R.id.publicVotingButton:
                if (checked) {
                    mVotingLengthSpinner.setEnabled(true);
                    mChosenVotingChoice = VotingChoice.PUBLIC;
                }
                break;


            case R.id.mutualFacebookFriendsButton:
                if (checked)
                {
                    mVotingLengthSpinner.setEnabled(true);
                    mChosenVotingChoice = VotingChoice.MUTUAL_FACEBOOK;
                }

                break;
        }
    }


    public enum VotingChoice {
        MUTUAL_FACEBOOK {
            public String toString() {
                return App.getContext().getResources().getText(R.string.mutual_facebook_friends).toString();
            }
            public String getLongStyle(){
                return App.getContext().getResources().getText(R.string.mutual_facebook_friends_long).toString();
            }
        },
        PUBLIC {
            public String toString() {
                return App.getContext().getResources().getText(R.string.public_voting).toString();
            }
            public String getLongStyle(){
                return App.getContext().getResources().getText(R.string.public_voting_long).toString();
            }
        },
        SELECTED {
            public String toString() {
                return App.getContext().getResources().getText(R.string.selected_judges).toString();
            }
            public String getLongStyle(){
                return App.getContext().getResources().getText(R.string.selected_judges).toString();
            }
        },
        NONE {
            public String toString() {
                return App.getContext().getResources().getText(R.string.no_voting_used).toString();
            }
            public String getLongStyle(){
                return App.getContext().getResources().getText(R.string.no_voting_used).toString();
            }
        };
        public abstract String getLongStyle();
    }


    public enum VotingLength {
        TWENTY_FOUR_HOURS {
            public String toString(Context context) {
                return context.getResources().getStringArray(R.array.VotingLengths)[0];
            }
        },
        THREE_DAYS {
            public String toString(Context context) {
                return context.getResources().getStringArray(R.array.VotingLengths)[1];
            }
        },
        ONE_WEEK {
            public String toString(Context context) {
                return context.getResources().getStringArray(R.array.VotingLengths)[2];
            }
        },
        ONE_MONTH {
            public String toString(Context context) {
                return context.getResources().getStringArray(R.array.VotingLengths)[3];
            }
        };
        public abstract String toString(Context context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle) {


        View view = inflater.inflate(R.layout.fragment_choose_voting, container, false);


        mVotingLengthSpinner = view.findViewById(R.id.votingLengthSpinner);
        RadioGroup votingTypeRadioGroup = view.findViewById(R.id.votingTypeRadioGroup);

        view.findViewById(R.id.noVotingButton).setOnClickListener(this);
        view.findViewById(R.id.publicVotingButton).setOnClickListener(this);
        view.findViewById(R.id.mutualFacebookFriendsButton).setOnClickListener(this);


        votingTypeRadioGroup.check(R.id.noVotingButton);
        mChosenVotingChoice = VotingChoice.NONE;


        ArrayAdapter<VotingLength> adapter = new VotingLengthAdapter(getActivity());


        // Specify the layout to use when the list of voting length choices appears
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        mVotingLengthSpinner.setAdapter(adapter);
        mVotingLengthSpinner.setEnabled(false);
        mVotingLengthSpinner.setSelection(0);


        return view;
    }



    public VotingChoice getChosenVotingChoice()
    {
        return mChosenVotingChoice;
    }
    public VotingLength getChosenVotingLength()
    {
        return (VotingLength)mVotingLengthSpinner.getSelectedItem();
    }





        public class VotingLengthAdapter extends ArrayAdapter<VotingLength> {
        private Context con;
        public VotingLengthAdapter (Context context) {
            super(context, 0, VotingLength.values());
            con = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CheckedTextView text= (CheckedTextView) convertView;

            if (text== null) {
                text = (CheckedTextView) LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_dropdown_item,  parent, false);
            }

            text.setText(getItem(position).toString(con));

            return text;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            CheckedTextView text = (CheckedTextView) convertView;

            if (text == null) {
                text = (CheckedTextView) LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_dropdown_item,  parent, false);
            }

            text.setText(getItem(position).toString(con));

            return text;
        }
    }




}