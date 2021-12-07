package org.scp.gymlog.room;

import androidx.room.TypeConverter;

import org.scp.gymlog.model.WeightSpecification;

import java.util.Date;

public class Converters {
    @TypeConverter
    public static Date toDate(Long dateLong){
        return dateLong == null ? null: new Date(dateLong);
    }

    @TypeConverter
    public static Long fromDate(Date date){
        return date == null ? null : date.getTime();
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
