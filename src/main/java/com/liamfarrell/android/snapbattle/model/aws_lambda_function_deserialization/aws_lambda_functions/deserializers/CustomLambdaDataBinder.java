
package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.deserializers;

import com.amazonaws.mobileconnectors.lambdainvoker.LambdaDataBinder;
import com.amazonaws.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.liamfarrell.android.snapbattle.model.Battle;
import com.liamfarrell.android.snapbattle.model.ReportedBattle;
import com.liamfarrell.android.snapbattle.model.ReportedComment;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;

public class CustomLambdaDataBinder implements LambdaDataBinder {
    private final Gson gson;

    /**
     * Constructs a Lambda Json binder.
     */
    public CustomLambdaDataBinder() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Battle.class, new BattleDeserializer());
        builder.registerTypeAdapter(ReportedBattle.class, new ReportedBattleDeserializer());
        builder.registerTypeAdapter(ReportedComment.class, new ReportedCommentDeserializer());
        builder.registerTypeAdapter(Boolean.class, new CustomBooleanTypeAdapter());
        builder.registerTypeAdapter(boolean.class, new CustomBooleanTypeAdapter());
        builder.registerTypeAdapter(Date.class, new GsonUTCDateAdapter());
       gson = builder.create();
    }

    @Override
    public <T> T deserialize(byte[] content, Class<T> clazz) {
        if (content == null) {
            return null;
        }
        Reader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content),
                StringUtils.UTF8));
        return gson.fromJson(reader, clazz);
    }

    @Override
    public byte[] serialize(Object object) {
        return gson.toJson(object).getBytes(StringUtils.UTF8);
    }
}