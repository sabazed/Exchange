package server;

enum Side {
    SELL, BUY
}

enum Status {
    CancelFail, OrderFail,
    Username, Instrument, Price, Quantity
}

enum Service {
    Gateway, Engine
}
