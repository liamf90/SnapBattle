package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.deserializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.liamfarrell.android.snapbattle.model.ReportedBattle;

import java.lang.reflect.Type;

public class ReportedBattleDeserializer implements JsonDeserializer<ReportedBattle> {
    @Override
    public ReportedBattle deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();
        int battleID = jsonObject.get("battleID").getAsInt();
        Integer rounds = jsonObject.get("rounds").getAsInt();
        String battleName = jsonObject.get("battle_name").getAsString();
        String challengerCognitoId = jsonObject.get("cognito_id_challenger").getAsString();
        String challengedCognitoId = jsonObject.get("cognito_id_challenged").getAsString();
        String challengerName = null;
        String challengerUsername= null;
        String challengedName= null;
        String challengedUsername = null;
        String thumbnailSignedUrl = null;

        if (jsonObject.has("name_challenger")&& !jsonObject.get("name_challenger").isJsonNull()) {
            challengerName = jsonObject.get("name_challenger").getAsString();
        }
        if (jsonObject.has("username_challenger")&& !jsonObject.get("username_challenger").isJsonNull()) {
            challengerUsername = jsonObject.get("username_challenger").getAsString();
        }
        if (jsonObject.has("name_challenged")&& !jsonObject.get("name_challenged").isJsonNull()) {
            challengedName = jsonObject.get("name_challenged").getAsString();
        }
        if (jsonObject.has("username_challenged") && !jsonObject.get("username_challenged").isJsonNull()) {
            challengedUsername = jsonObject.get("username_challenged").getAsString();
        }
        if (jsonObject.has("thumbnail_signed_url") && !jsonObject.get("thumbnail_signed_url").isJsonNull()) {
            thumbnailSignedUrl = jsonObject.get("thumbnail_signed_url").getAsString();
        }
        ReportedBattle b = new ReportedBattle(battleID, challengerCognitoId, challengedCognitoId, battleName, rounds, challengerUsername, challengerName, challengedUsername, challengedName,thumbnailSignedUrl );

        return b;
    }
}
