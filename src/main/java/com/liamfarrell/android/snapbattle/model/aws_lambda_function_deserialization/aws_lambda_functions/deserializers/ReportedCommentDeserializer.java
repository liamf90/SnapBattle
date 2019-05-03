package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.deserializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.liamfarrell.android.snapbattle.model.ReportedComment;

import java.lang.reflect.Type;
import java.util.Date;

public class ReportedCommentDeserializer implements JsonDeserializer<ReportedComment> {

    @Override
    public ReportedComment deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        final JsonObject jsonObject = json.getAsJsonObject();

        int commentId = jsonObject.get("comment_id").getAsInt();
        String comment = jsonObject.get("comment").getAsString();
        Integer battleid = jsonObject.get("battleid").getAsInt();
        Date timeOfReport = JSONDeserializerHelperMethods.getDateFromString(jsonObject.get("time_of_report").getAsString());
        int complaintCount = jsonObject.get("complaint_count").getAsInt();
        String userReportedCognitoId = jsonObject.get("user_reported_cognito_id").getAsString();

        String userReportedUsername  = null;
        String userReportedName = null;
        if (jsonObject.has("Username")&& !jsonObject.get("Username").isJsonNull()) {
            userReportedUsername = jsonObject.get("Username").getAsString();
        }
        if (jsonObject.has("user_reported_name")&& !jsonObject.get("user_reported_name").isJsonNull()) {
            userReportedName = jsonObject.get("user_reported_name").getAsString();
        }
        ReportedComment rc = new ReportedComment(commentId, userReportedUsername, userReportedName, battleid, false, comment,null ,userReportedCognitoId,0, null, timeOfReport, complaintCount, userReportedUsername, userReportedName);
        return rc;
    }
}
