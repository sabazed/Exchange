package server;

enum Side {
    SELL, BUY
}

enum Status {
    CancelFail, OrderFail, FatalFail,
    Username, Instrument, Price, Quantity
}

enum Service {
    Gateway, Engine
}
