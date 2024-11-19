package com.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
public enum PropertyUnitEnum {
    N("N", "mg/kg"),
    L("P2O5", "mg/kg"),
    K("K2O", "mg/kg"),
    TEMPERATURE("温度", "°C"),
    HUMIDITY("湿度", "RH%"),
    TEMPERATURE_TU("温度（土壤）", "°C"),
    HUMIDITY_TU("湿度（土壤）", "RH%"),
    EC("EC", "ms/cm"),
    PH("PH", ""),
    CO2("Co₂", "ppm"),
    LIGHT("光照", "Lux"),
    WIND("风速", "m/s"),
    EVAPORATION("蒸发量", "mm"),
    RAIN("降雨量", "mm"),
    PA("气压", "Pa"),
    PAR("光合辐射", "PAR");


    @Getter
    private final String name;

    @Getter
    private final String value;

    public static PropertyUnitEnum getTaskValue(String key) {
        Optional<PropertyUnitEnum> optional = Arrays.stream(values())
                                            .filter(instance -> instance.value.equals(key)).limit(1)
                                            .findFirst();

        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

}
