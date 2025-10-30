package co.yiiu.pybbs.config.websocket;

import co.yiiu.pybbs.model.Collect;
import co.yiiu.pybbs.model.vo.UserWithWebSocketVO;
import co.yiiu.pybbs.service.ICollectService;
import co.yiiu.pybbs.service.INotificationService;
import co.yiiu.pybbs.service.impl.CollectService;
import co.yiiu.pybbs.service.impl.NotificationService;
import co.yiiu.pybbs.util.Message;
import co.yiiu.pybbs.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tomoya.
 * Copyright (c) 2018, All Rights Reserved.
 * https://atjiu.github.io
 */
@ServerEndpoint(value = "/websocket", encoders = MessageEncoder.class, decoders = MessageDecoder.class)
@Component
public class MyWebSocket {

    private final static Logger log = LoggerFactory.getLogger(MyWebSocket.class);

    //在线人数
    private final static AtomicInteger online = new AtomicInteger(0);
    //所有的对象，用于群发
    public static Map<Session, UserWithWebSocketVO> webSockets = new ConcurrentHashMap<>();
    // 按组群发
    // groupName -> Set<Session>
    public static Map<String, Set<Session>> groups = new ConcurrentHashMap<>();


    //建立连接
    @OnOpen
    public void onOpen(Session session) {
        online.incrementAndGet();
        webSockets.put(session, new UserWithWebSocketVO());
    }

    //连接关闭
    @OnClose
    public void onClose(Session session) {
            online.decrementAndGet();
        groups.values().forEach(sessions -> sessions.remove(session));
        webSockets.remove(session);
    }

    //收到客户端的消息
    @OnMessage
    public void onMessage(Message message, Session session) {
        if (message != null) {
            switch (message.getType()) {
                case "bind":
                    bind(message, session);
                    break;
                case "notReadCount":
                    fetchNotReadCount(message, session);
                    break;
                default:
                    break;
            }
        }
    }

    // 将登录用户与websocket绑定
    private void bind(Message message, Session session) {
        try {
            Integer userId = Integer.parseInt(((Map) (message.getPayload())).get("userId").toString());
            String username = ((Map) (message.getPayload())).get("username").toString();
            // 用户Id和用户名绑定到websocket对象上
            UserWithWebSocketVO userWithWebSocketVO = webSockets.get(session);
            userWithWebSocketVO.setUserId(userId);
            userWithWebSocketVO.setUsername(username);
            // 更新map
            webSockets.put(session, userWithWebSocketVO);
            session.getBasicRemote().sendObject(new Message("bind", null));
            // 自动恢复订阅，根据用户id查询该用户收藏的所有话题，然后加入对应的话题群组
            ICollectService collectService = SpringContextUtil.getBean(CollectService.class);
            List<Map<String, Object>> topics = collectService.selectByUserId(userId, 1, Integer.MAX_VALUE).getRecords();
            for (Map<String, Object> topic : topics) {
                Integer topicId = (Integer) topic.get("id");
                MyWebSocket.joinGroup("topic_" + topicId, userId);
            }
        } catch (IOException | EncodeException e) {
            log.error("发送ws消息失败, 异常信息: {}", e.getMessage());
        }
    }

    // 获取用户的未读消息数
    private static void fetchNotReadCount(Message message, Session session) {
        try {
            INotificationService notificationService = SpringContextUtil.getBean(NotificationService.class);
            // 通过session找到用户id，然后查询未读消息数
            long countNotRead = notificationService.countNotRead(MyWebSocket.webSockets.get(session).getUserId());
            session.getBasicRemote().sendObject(new Message("notification_notread", countNotRead));
        } catch (IOException | EncodeException e) {
            log.error("发送ws消息失败, 异常信息: {}", e.getMessage());
        }
    }

    // 提供一个方法用于根据用户id查询session
    private static Session selectSessionByUserId(Integer userId) {
        return webSockets.entrySet().stream().filter(x -> x.getValue().getUserId().equals(userId)).map(Map.Entry::getKey).findFirst().orElse(null);
    }

    // 提供一个方法用于给指定用户发送消息
    public static void emit(Integer userId, Message message) {
        try {
            Session session = selectSessionByUserId(userId);
            if (session != null) session.getBasicRemote().sendObject(message);
        } catch (IOException | EncodeException e) {
            log.error("发送ws消息失败, 异常信息：{}", e.getMessage());
        }
    }

    // 提供一个方法用于群发消息
    public static void emitAll(Message message) {
        webSockets.keySet().forEach(session -> {
            try {
                session.getBasicRemote().sendObject(message);
            } catch (IOException | EncodeException e) {
                log.error("发送ws消息失败, 异常信息：{}", e.getMessage());
            }
        });
    }

    public static void joinGroup(String group, Integer userId) {
        // “在 groups 这张表中找到名为 group 的集合；
        //如果没有就新建一个线程安全的集合；
        //然后把当前的 Session s 加进去。”
        Session s = selectSessionByUserId(userId);
        groups.computeIfAbsent(group, g -> ConcurrentHashMap.newKeySet()).add(s);
    }

    public static void leaveGroup(String group, Integer userId) {
        Set<Session> set = groups.get(group);
        Session s = selectSessionByUserId(userId);
        if (set != null) {
            set.remove(s);
            if (set.isEmpty()) groups.remove(group);
        }
    }

    public static void broadcast(String groupId, Message msg, Integer excludeUserId) {
        Set<Session> set = groups.get(groupId);
        if (set == null) return;
        for (Session s : set) {
            if (!s.isOpen()) continue;
            Integer userId = webSockets.get(s).getUserId();
            if (excludeUserId != null && excludeUserId.equals(userId)) continue;
            s.getAsyncRemote().sendObject(msg);
        }
    }
}
