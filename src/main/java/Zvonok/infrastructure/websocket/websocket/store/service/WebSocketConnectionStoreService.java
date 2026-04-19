package Zvonok.infrastructure.websocket.websocket.store.service;

import Zvonok.infrastructure.websocket.websocket.store.entity.connectionInfo.ConnectionInfo;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Getter
public class WebSocketConnectionStoreService {

    // sessionId -> ConnectionInfo
    private final Map<String, ConnectionInfo> connections = new ConcurrentHashMap<>();

    // username -> sessionId set
    private final Map<String, Set<String>> userSessions = new ConcurrentHashMap<>();

    public void addConnection(String sessionId, String username) {
        ConnectionInfo info = new ConnectionInfo(sessionId, username);
        connections.put(sessionId, info);
        userSessions.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
    }

    public void removeConnection(String sessionId) {
        ConnectionInfo info = connections.remove(sessionId);
        if (info != null) {
            Set<String> sessions = userSessions.get(info.getUsername());
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    userSessions.remove(info.getUsername());
                }
            }
        }
    }

    public void addSubscription(String sessionId, String topic) {
        ConnectionInfo info = connections.get(sessionId);
        if (info != null) {
            info.addSubscription(topic);
        }
    }

    public void removeSubscription(String sessionId, String topic) {
        ConnectionInfo info = connections.get(sessionId);
        if (info != null) {
            info.removeSubscription(topic);
        }
    }

    public boolean isUserOnline(String username) {
        Set<String> sessions = userSessions.get(username);
        return sessions != null && !sessions.isEmpty();
    }

}
