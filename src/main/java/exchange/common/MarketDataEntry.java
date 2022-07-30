package exchange.common;

import java.math.BigDecimal;

public class MarketDataEntry {

    private final Instrument instrument;
    private final BigDecimal buy;
    private final BigDecimal sell;

    public MarketDataEntry(Instrument instrument, BigDecimal buy, BigDecimal sell) {
        this.instrument = instrument;
        this.buy = buy;
        this.sell = sell;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public BigDecimal getBuy() {
        return buy;
    }

    public BigDecimal getSell() {
        return sell;
    }

    @Override
    public String toString() {
        return "MarketDataEntry{" +
                "instrument=" + instrument +
                ", buy=" + buy +
                ", sell=" + sell +
                '}';
    }
}