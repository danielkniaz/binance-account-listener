package io.prada.listener.dto.enums;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SideType {
    BUY(1),
    SELL(-1);

    private final Integer dir;

    public static SideType of(String str) {
        return Arrays.stream(SideType.values())
            .filter(side -> str.equals(side.name())).findFirst().orElseThrow();
    }
}
