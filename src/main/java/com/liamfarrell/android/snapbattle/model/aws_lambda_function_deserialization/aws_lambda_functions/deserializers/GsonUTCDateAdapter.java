package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.deserializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class GsonUTCDateAdapter implements JsonDeserializer<Date>, JsonSerializer<Date> {

    private final DateFormat dateFormat;

    public GsonUTCDateAdapter() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override public synchronized Date deserialize(JsonElement jsonElement,Type type, JsonDeserializationContext jsonDeserializationContext) {
        try {
            return dateFormat.parse(jsonElement.getAsString());
        } catch (ParseException e) {
            throw new JsonParseException(e);
        }
    }

    @Override public synchronized JsonElement serialize(Date date,Type type,JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(dateFormat.format(date));
    }
}