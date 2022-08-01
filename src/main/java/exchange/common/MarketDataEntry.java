package exchange.common;

import java.math.BigDecimal;
import java.util.Objects;

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