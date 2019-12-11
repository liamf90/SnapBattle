package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.deserializers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class JSONDeserializerHelperMethods {
    public static Date getDateFromString(String dateFromMysql)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return sdf.parse(dateFromMysql);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        catch (java.lang.NullPointerException e){
            return null;
        }

        return null;
    }

    public static boolean getBooleanFromMysqlBool(int mysqlBoolean)
    {
        if (mysqlBoolean == 1) {
            return true; }
        else {
            return false; }
    }

}
