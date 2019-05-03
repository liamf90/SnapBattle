package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.deserializers;

import java.lang.reflect.Type;
import com.google.gson.*;
public class CustomBooleanTypeAdapter implements JsonDeserializer<Boolean>
{
    public Boolean deserialize(JsonElement json, Type typeOfT,
                               JsonDeserializationContext context) throws JsonParseException
    {
        int code = json.getAsInt();
        return code == 0 ? false :
                code == 1 ? true :
                        null;
    }
}