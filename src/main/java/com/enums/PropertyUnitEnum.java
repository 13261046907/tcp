package com.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
public enum PropertyUnitEnum {
    N("氮", "mg/kg"),
    L("磷", "mg/kg"),
    K("钾", "mg/kg"),
    TEMPERATURE("温度", "°C"),
    HUMIDITY("湿度", "RH%"),
    EC("EC", "mS/cm"),
    PH("PH", ""),
    CO2("co2", "ppm"),
    LIGHT("光照", "Lux,lx"),
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
