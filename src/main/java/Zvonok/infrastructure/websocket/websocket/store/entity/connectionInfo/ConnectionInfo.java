package Zvonok.infrastructure.websocket.websocket.store.entity.connectionInfo;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
@Getter
@Setter
public class ConnectionInfo {
    private final String sessionId;
    private final String username;
    private final Set<String> subscriptions = new HashSet<>();

    public ConnectionInfo(String sessionId, String username) {
        this.sessionId = sessionId;
        this.username = username;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUsername() {
        return username;
    }

    public Set<String> getSubscriptions() {
        return subscriptions;
    }

    public void addSubscription(String topic) {
        subscriptions.add(topic);
    }

    public void removeSubscription(String topic) {
        subscriptions.remove(topic);
    }
}