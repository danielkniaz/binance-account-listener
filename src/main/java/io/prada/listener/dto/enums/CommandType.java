package io.prada.listener.dto.enums;

public enum CommandType {
    SL,
    TP,
    LMT,
    //above ^ only pending

    //below \/ only market
    BUY,
    SELL,
    CLOSE
//TODO: do we need them all ?
}
