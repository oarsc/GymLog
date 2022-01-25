package org.scp.gymlog.model;

import static org.scp.gymlog.util.WeightUtils.toKilograms;
import static org.scp.gymlog.util.WeightUtils.toPounds;

import java.math.BigDecimal;
import java.util.function.Function;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Weight implements Comparable<Weight>{
    protected final BigDecimal value;
    protected final boolean internationalSystem;

    public BigDecimal getValue(boolean internationalSystem) {
        return internationalSystem? toKg() : toLbs();
    }

    public BigDecimal getValue(boolean internationalSystem, int scale) {
        return internationalSystem? toKg(scale) : toLbs(scale);
    }

    public BigDecimal toKg() {
        return internationalSystem? value : toKilograms(value);
    }

    public BigDecimal toLbs() {
        return internationalSystem? toPounds(value) : value;
    }

    public BigDecimal toKg(int scale) {
        return internationalSystem? value : toKilograms(value, scale);
    }

    public BigDecimal toLbs(int scale) {
        return internationalSystem? toPounds(value, scale) : value;
    }

    public Weight add(Weight weight) {
        if (weight.internationalSystem != internationalSystem) {
            throw new ArithmeticException("Can't operate with different units");
        }
        return new Weight(value.add(weight.value), internationalSystem);
    }

    public Weight add(Weight weight, boolean internationalSystem) {
        if (internationalSystem)
            return new Weight(toKg().add(weight.toKg()), true);
        else
            return new Weight(toLbs().add(weight.toLbs()), false);
    }

    public Weight subtract(Weight weight) {
        if (weight.internationalSystem != internationalSystem) {
            throw new ArithmeticException("Can't operate with different units");
        }
        return new Weight(value.subtract(weight.value), internationalSystem);
    }

    public Weight subtract(Weight weight, boolean internationalSystem) {
        if (internationalSystem)
            return new Weight(toKg().subtract(weight.toKg()), true);
        else
            return new Weight(toLbs().subtract(weight.toLbs()), false);
    }

    public Weight op(Function<BigDecimal, BigDecimal> operation) {
        return new Weight(
                operation.apply(value),
                internationalSystem
            );
    }

    @Override
    public int compareTo(Weight o) {
        return value.compareTo(o.value);
    }
}
