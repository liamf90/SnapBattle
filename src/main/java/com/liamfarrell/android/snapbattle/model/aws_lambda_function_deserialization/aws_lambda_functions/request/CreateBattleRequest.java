package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request;

import java.util.List;

public class CreateBattleRequest
{
    private String challengedCognitoID;
    private String challengedFacebookID;
    private String votingChoice;
    private String votingLength;
    private int numberOfRounds;
    private String battleName;
    private List<String> selectedJudgesArray;

    public String getChallengedCognitoID() {
        return challengedCognitoID;
    }

    public void setChallengedCognitoID(String challengedCognitoID) {
        this.challengedCognitoID = challengedCognitoID;
    }

    public String getChallengedFacebookID() {
        return challengedFacebookID;
    }

    public void setChallengedFacebookID(String challengedFacebookID) {
        this.challengedFacebookID = challengedFacebookID;
    }

    public String getVotingChoice() {
        return votingChoice;
    }

    public void setVotingChoice(String votingChoice) {
        this.votingChoice = votingChoice;
    }

    public String getVotingLength() {
        return votingLength;
    }

    public void setVotingLength(String votingLength) {
        this.votingLength = votingLength;
    }

    public int getNumberOfRounds() {
        return numberOfRounds;
    }

    public void setNumberOfRounds(int numberOfRounds) {
        this.numberOfRounds = numberOfRounds;
    }

    public String getBattleName() {
        return battleName;
    }

    public void setBattleName(String battleName) {
        this.battleName = battleName;
    }

    public List<String> getSelectedJudgesArray() {
        return selectedJudgesArray;
    }

    public void setSelectedJudgesArray(List<String> selectedJudgesArray) {
        this.selectedJudgesArray = selectedJudgesArray;
    }
}
