package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

import com.liamfarrell.android.snapbattle.model.Battle;
import com.liamfarrell.android.snapbattle.model.User;

import java.util.List;

public class GetUsersBattlesResponse
{
    private List<Battle> user_battles;
    private User user_profile;

    public List<Battle> getUser_battles() {
        return user_battles;
    }

    public void setUserBattles(List<Battle> user_battles) {
        this.user_battles = user_battles;
    }

    public User getUser_profile() {
        return user_profile;
    }

    public void setUserProfile(User user_profile) {
        this.user_profile = user_profile;
    }
}
