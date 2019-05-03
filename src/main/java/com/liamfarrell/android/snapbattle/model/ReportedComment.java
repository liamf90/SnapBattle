package com.liamfarrell.android.snapbattle.model;

import com.liamfarrell.android.snapbattle.model.Comment;

import java.util.Date;

public class ReportedComment extends Comment
{
    private boolean mCommentIgnored = false;
    private boolean mUserIsBanned = false;
    private boolean mCommentDeleted = false;

    private Date mTimeOfReport;
    private int mComplaintCount;
    private String mUserReportedUsername;
    private String mUserReportedName;

    public ReportedComment(int commentid, String username, String name, int battleId, boolean isDeleted, String comment, Date time, String cognitoIdCommenter, int commenterProfilePicCount, String commenterProfilePicSmallSignedUrl, Date timeOfReport, int complaintCount, String userReportedUsername, String userReportedName) {
        super(commentid, username, name, battleId, isDeleted, comment, time, cognitoIdCommenter, commenterProfilePicCount, commenterProfilePicSmallSignedUrl);
        mTimeOfReport = timeOfReport;
        mComplaintCount = complaintCount;
        mUserReportedUsername = userReportedUsername;
        mUserReportedName = userReportedName;
    }

    public Date getTimeOfReport() {
        return mTimeOfReport;
    }

    public int getComplaintCount() {
        return mComplaintCount;
    }

    public String getUserReportedUsername() {
        return mUserReportedUsername;
    }

    public String getUserReportedName() {
        return mUserReportedName;
    }

    public boolean isCommentIgnored() {
        return mCommentIgnored;
    }

    public void setCommentIgnored(boolean commentIgnored) {
        mCommentIgnored = commentIgnored;
    }

    public boolean isUserIsBanned() {
        return mUserIsBanned;
    }

    public void setUserIsBanned(boolean userIsBanned) {
        mUserIsBanned = userIsBanned;
    }

    public boolean isCommentDeleted() {
        return mCommentDeleted;
    }

    public void setCommentDeleted(boolean commentDeleted) {
        mCommentDeleted = commentDeleted;
    }
}
