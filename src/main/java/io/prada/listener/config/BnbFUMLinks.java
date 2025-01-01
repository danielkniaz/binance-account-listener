package io.prada.listener.config;

public interface BnbFUMLinks {

    String bnbUF = "https://fapi.binance.com/";
    String listenKey = "fapi/v1/listenKey";

    String header = "X-MBX-APIKEY";

    String wss = "wss://fstream.binance.com/ws/";

    String balance = "fapi/v2/account?";
    String orders = "fapi/v1/openOrders?";
    String positions = "fapi/v2/positionRisk?";

}
