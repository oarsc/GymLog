package org.scp.gymlog.room;

import androidx.room.TypeConverter;

import org.scp.gymlog.model.WeightSpecification;

import java.util.Calendar;
import java.util.Date;

public class Converters {
    @TypeConverter
    public static Calendar toDate(Long dateLong){
        if (dateLong == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateLong);
        return cal;
    }

    @TypeConverter
    public static Long fromDate(Calendar cal){
        return cal == null ? null : cal.getTimeInMillis();
    }

    @TypeConverter
    public static short fromWeightSpecification(WeightSpecification weightSpecification){
        return (short) weightSpecification.ordinal();
    }

    @TypeConverter
    public static WeightSpecification toWeightSpecification(short weightSpecification){
        return WeightSpecification.values()[weightSpecification];
    }
}
