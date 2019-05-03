/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liamfarrell.android.snapbattle.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.liamfarrell.android.snapbattle.R;


/**
 * A view containing controls for a MediaPlayer. Typically contains the
 * buttons like "Play/Pause", "Rewind", "Fast Forward" and a progress
 * slider. It takes care of synchronizing the controls with the state
 * of the MediaPlayer.
 * <p>
 * The way to use this class is to instantiate it programatically.
 * The MediaController will create a default set of controls
 * and put them in a window floating above your application. Specifically,
 * the controls will float above the view specified with setAnchorView().
 * The window will disappear if left idle for three seconds and reappear
 * when the user touches the anchor view.
 * <p>
 * Functions like show() and hide() have no effect when MediaController
 * is created in an xml layout.
 *
 * MediaController will hide and
 * show the buttons according to these rules:
 * <ul>
 * <li> The "previous" and "next" buttons are hidden until setPrevNextListeners()
 *   has been called
 * <li> The "previous" and "next" buttons are visible but disabled if
 *   setPrevNextListeners() was called with null listeners
 * <li> The "rewind" and "fastforward" buttons are shown unless requested
 *   otherwise by using the MediaController(Context, boolean) constructor
 *   with the boolean set to false
 * </ul>
 */
public class VideoControllerView extends VideoController {

    private TextView            mLikeCountTextView;
    private TextView            mDislikeCountTextView;
    private Button mCommentsButton, mVoteButton, mReportButton;
    private ImageButton mLikeButton;
    private ImageButton mDislikeButton;




    public VideoControllerView(Context context, AttributeSet attrs) {

        super(context, attrs);
    }

    public VideoControllerView(Context context, boolean useFastForward) {
        super(context, useFastForward);
    }

    public VideoControllerView(Context context) {
        super(context);


    }






    protected int getMediaControllerLayout()
    {
        return R.layout.media_controller_battle;
    }
    public void makeLikeDislikeButtonsEnabled()
    {
        mLikeButton.setEnabled(true);
        mDislikeButton.setEnabled(true);
    }
    public void makeLikeDislikeButtonsNotEnabled()
    {
        mLikeButton.setEnabled(false);
        mDislikeButton.setEnabled(false);
    }

    public void setCommentsCallback(View.OnClickListener listener)
    {
        mCommentsButton.setOnClickListener(listener);
    }
    public void setLikeCallback(View.OnClickListener listener)
    {
        mLikeButton.setOnClickListener(listener);
    }
    public void setDislikeCallback(View.OnClickListener listener)
    {
        mDislikeButton.setOnClickListener(listener);
    }
    public void setLikeButtonLiked()
    {
        mLikeButton.setBackgroundResource(R.drawable.thumb_up);
    }

    public void setDisLikeButton()
    {
        //mDislikeButton.setBackgroundTintMode(Mod);
        mDislikeButton.setBackgroundResource(R.drawable.thumb_down);
    }
    public void setLikeButtonUnLiked()
    {
        mLikeButton.setBackgroundResource(R.drawable.thumb_up_outline);
    }
    public void setDisLikeButtonUndisliked()
    {
        mDislikeButton.setBackgroundResource(R.drawable.thumb_down_outline);
    }
    public void setVoteButtonCallback(View.OnClickListener listener)
    {
        mVoteButton.setOnClickListener(listener);
    }
    public void setReportButtonCallback(View.OnClickListener listener)
    {
        mReportButton.setOnClickListener(listener);
    }
    public void setVoteButtonVoted()
    {
        mVoteButton.setText(R.string.voted);
        mVoteButton.setEnabled(false);
        mVoteButton.setBackground(getResources().getDrawable(R.drawable.vote_button_disabled));
        mVoteButton.setTextColor(getResources().getColor(R.color.vote_disabled));
    }
    public void setVoteButtonVisible()
    {
         mVoteButton.setVisibility(VISIBLE);
         mVoteButton.setEnabled(true);
    }

    public void setVotingButtonInvisible()
    {
        mVoteButton.setVisibility(INVISIBLE);
    }

    public void setVoteButtonDisabled()
    {
        mVoteButton.setEnabled(false);
    }

    public void setReportButtonReporting()
    {
        mReportButton.setText(R.string.reporting);
    }
    public void setReportButtonReported()
    {
        mReportButton.setText(R.string.reported);
    }


    public Button getVoteButton()
    {
        return mVoteButton;
    }

    public void setLikesCount(int count)
    {
        mLikeCountTextView.setText(Integer.toString(count));
    }
    public void setDislikesCount(int count)
    {
        mDislikeCountTextView.setText(Integer.toString(count));
    }

    public void setCommentsButtonText(String text)
    {
        mCommentsButton.setText(text);
    }

    protected void initControllerView(View v) {
        super.initControllerView(v);

        mCommentsButton = v.findViewById(R.id.viewCommentsButton);
        mVoteButton = v.findViewById(R.id.voteButton);

        mLikeButton = v.findViewById(R.id.likeButton);
       // mLikeButton.setOnClickListener(likeOnClickListener);
        mDislikeButton = v.findViewById(R.id.dislikeButton);
        //mDislikeButton.setOnClickListener(dislikeOnClickListener);
        mLikeCountTextView = v.findViewById(R.id.likeCountTextView);
        mDislikeCountTextView = v.findViewById(R.id.dislikeCountTextView);
        mReportButton = v.findViewById(R.id.reportButton);

    }



}