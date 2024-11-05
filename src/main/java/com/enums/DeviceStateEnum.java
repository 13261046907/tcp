package com.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
public enum DeviceStateEnum {
    online("1", "online"),
    offline("0", "offline");

    @Getter
    private final String name;

    @Getter
    private final String value;

    public static DeviceStateEnum getStateValue(String key) {
        Optional<DeviceStateEnum> optional = Arrays.stream(values())
                                            .filter(instance -> instance.value.equals(key)).limit(1)
                                            .findFirst();

        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

}
