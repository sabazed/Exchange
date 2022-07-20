package server;

enum Side {

    SELL(1), BUY(-1);

    private final int val;

    Side(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

}

enum Status {
    CancelFail, OrderFail,
    Username, Instrument, Price, Quantity
}