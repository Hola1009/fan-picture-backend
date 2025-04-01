package com.fancier.picture.backend.websocket;

import cn.hutool.json.JSONUtil;
import com.fancier.picture.backend.config.JsonConfig;
import com.fancier.picture.backend.model.user.vo.UserVO;
import com.fancier.picture.backend.websocket.disruptor.PictureEditEventProducer;
import com.fancier.picture.backend.websocket.model.PictureEditActionEnum;
import com.fancier.picture.backend.websocket.model.PictureEditMessageTypeEnum;
import com.fancier.picture.backend.websocket.model.PictureEditRequestMessage;
import com.fancier.picture.backend.websocket.model.PictureEditResponseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Component
public class PictureEditHandler extends TextWebSocketHandler {


    private final Map<Long, Long> pictureIdUserIdMap = new ConcurrentHashMap<>();

    private final Map<Long, Set<WebSocketSession>> pictureSessions = new ConcurrentHashMap<>();

    @Resource
    private PictureEditEventProducer pictureEditEventProducer;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        UserVO user = (UserVO) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        pictureSessions.putIfAbsent(pictureId, ConcurrentHashMap.newKeySet());
        pictureSessions.get(pictureId).add(session);

        PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
        responseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        responseMessage.setMessage(String.format("用户 %s 已连接", user.getUserName()));
        responseMessage.setUser(user);
        broadcast(pictureId, responseMessage);
    }

    /**
     * <h1 color="#01b486">核心方法</h1>
     */
    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        PictureEditRequestMessage requestMessage = JSONUtil.toBean(message.getPayload(), PictureEditRequestMessage.class);
        UserVO user = (UserVO) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");

        pictureEditEventProducer.publishEvent(requestMessage, session, user, pictureId);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        UserVO user = (UserVO) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);

        if (sessionSet != null) {
            sessionSet.remove(session);
            if (sessionSet.isEmpty()) {
                pictureSessions.remove(pictureId);
            }
        }

        handleExitEditMessage(user, pictureId);

        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        pictureEditResponseMessage.setMessage(String.format("用户 %s 已断开连接", user.getUserName()));
        pictureEditResponseMessage.setUser(user);

        broadcast(pictureId, pictureEditResponseMessage);
    }


    public void handleEnterEditMessage(UserVO user, Long pictureId) throws IOException {
        // 图片没有被用户编辑才进入编辑
        if (!pictureIdUserIdMap.containsKey(pictureId)) {
            pictureIdUserIdMap.put(pictureId, user.getId());

            // 构造响应
            PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
            responseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
            responseMessage.setMessage(String.format("用户 %s 已进入编辑", user.getUserName()));
            responseMessage.setUser(user);

            // 广播
            broadcast(pictureId, responseMessage);
        }
    }

    public void handleEditActionMessage(UserVO user, Long pictureId, String editAction, WebSocketSession session) throws IOException {
        Long editingUserId = pictureIdUserIdMap.get(pictureId);
        PictureEditActionEnum actionEnum = PictureEditActionEnum.of(editAction);
        if (actionEnum == null) {
            return;
        }

        if (editingUserId != null && !Objects.equals(editingUserId, user.getId())) {
            // 构造响应
            PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
            responseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
            responseMessage.setEditAction(editAction);
            responseMessage.setMessage(String.format("用户 %s 执行了 %s 操作", user.getUserName(), actionEnum.getText()));
            responseMessage.setUser(user);
            broadcast(pictureId, responseMessage, session);
        }
    }

    public void handleExitEditMessage(UserVO user, Long pictureId) throws IOException {
        Long editingUserId = pictureIdUserIdMap.get(pictureId);
        if (editingUserId != null && Objects.equals(editingUserId, user.getId())) {
            pictureIdUserIdMap.remove(pictureId);

            PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
            responseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
            responseMessage.setMessage(String.format("用户 %s 退出编辑操作", user.getUserName()));
            responseMessage.setUser(user);
            broadcast(pictureId, responseMessage);
        }
    }

    private void broadcast(Long pictureId, PictureEditResponseMessage message, WebSocketSession excludeSession) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(JsonConfig.getModule());

        String body = objectMapper.writeValueAsString(message);
        TextMessage textMessage = new TextMessage(body);

        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);

        for (WebSocketSession webSocketSession : sessionSet) {
            if (webSocketSession.equals(excludeSession)) continue;
            if (webSocketSession.isOpen()) {
                webSocketSession.sendMessage(textMessage);
            }
        }

    }

    private void broadcast(Long pictureId, PictureEditResponseMessage message) throws IOException {
        broadcast(pictureId, message, null);
    }

}
