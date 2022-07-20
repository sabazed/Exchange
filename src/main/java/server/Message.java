package server;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @Type(value = Order.class, name = "Order"),
        @Type(value = Cancel.class, name = "Cancel")
})
public interface Message {

    String getSession();

    void setSession(String session);

    String getClientId();

    long getGlobalId();

}
