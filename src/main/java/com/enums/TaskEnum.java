package com.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@AllArgsConstructor
public enum TaskEnum {
    TASK1("2", "0 0/2 * * * ?","2分钟"),
    TASK2("5", "0 0/5 * * * ?","5分钟"),
    TASK3("10", "0 0/10 * * * ?","10分钟"),
    TASK4("30", "0 0/30 * * * ?","30分钟"),
    TASK5("60", "0 0 */1 * * ?","1小时"),
    TASK6("1440", "0 0 1 * * ?","每天凌晨1点"),
    /**
     * 开关类型
     */
    TASK7("10008", "0 0 8 * * ?","每天早上8点"),
    TASK8("100020", "0 0 20 * * ?","每天晚上8点")
    ;

    @Getter
    private final String value;

    @Getter
    private final String key;

    @Getter
    private final String name;

    public static TaskEnum getTaskKey(String value) {
        Optional<TaskEnum> optional = Arrays.stream(values())
                .filter(instance -> instance.value.equals(value)).limit(1)
                .findFirst();

        if (optional.isPresent()) {
         return optional.get();
        }
        throw null;
    }

    public static TaskEnum getTaskValue(String key) {
        Optional<TaskEnum> optional = Arrays.stream(values())
                                            .filter(instance -> instance.key.equals(key)).limit(1)
                                            .findFirst();

        if (optional.isPresent()) {
            return optional.get();
        }
        throw null;
    }

}
