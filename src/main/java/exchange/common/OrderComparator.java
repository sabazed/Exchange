package exchange.common;

import exchange.messages.Order;

import java.util.Comparator;

class OrderComparator implements Comparator<Order> {

    private final int reversed;

    public OrderComparator(boolean reverse) {
        this.reversed = reverse ? -1 : 1;
    }

    @Override
    public int compare(Order o1, Order o2) {
        // Comparison priority: price - timestamp - id
        int temp = o1.getPrice().compareTo(o2.getPrice());
        if (temp == 0) {
            if (!o1.getInstant().equals(o2.getInstant()))
                return o1.getInstant().isBefore(o2.getInstant()) ? -1 : 1;
            else return Long.compare(o1.getGlobalId(), o2.getGlobalId());
        }
        return temp * reversed;
    }

}
