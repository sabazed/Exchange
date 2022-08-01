package exchange.common;

import java.math.BigDecimal;
import java.util.Objects;

public class MarketDataEntry {

    private final Instrument instrument;
    private final BigDecimal buy;
    private final BigDecimal sell;
    private final BigDecimal lastTrade;

    public MarketDataEntry(Instrument instrument, BigDecimal buy, BigDecimal sell, BigDecimal lastTrade) {
        this.instrument = instrument;
        this.buy = buy;
        this.sell = sell;
        this.lastTrade = lastTrade;
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

    public BigDecimal getLastTrade() {
        return lastTrade;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(instrument);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof MarketDataEntry entry && instrument.equals(entry.instrument);
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