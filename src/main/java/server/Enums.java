package server;

enum Side {
    SELL, BUY
}

enum Status {
    Cancel, Trade, List,
    Fail, CancelFail, OrderFail, ListFail,
    InvalidUser, InvalidInstr, InvalidPrice, InvalidQty
}

enum Service {
    Gateway, Engine
}
