package exchange.messages;

import java.math.BigDecimal;

public class Trade implements Message {

    private String session;
    private String clientId;
    private BigDecimal qty;

    public Trade(Order order) {
        session = order.getSession();
        qty = order.getQty();
        clientId = order.getClientId();
    }

    @Override
    public String getSession() {
        return session;
    }

    @Override
    public void setSession(String session) {
        this.session = session;
    }

    public BigDecimal getQty() {
        return qty;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "session='" + session + '\'' +
                ", clientId='" + clientId + '\'' +
                ", qty=" + qty +
                '}';
    }
}
