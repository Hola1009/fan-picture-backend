package com.fancier.picture.backend.websocket.disruptor;

import cn.hutool.json.JSONUtil;
import com.fancier.picture.backend.model.user.vo.LoginUserVO;
import com.fancier.picture.backend.websocket.PictureEditHandler;
import com.fancier.picture.backend.websocket.model.PictureEditMessageTypeEnum;
import com.fancier.picture.backend.websocket.model.PictureEditRequestMessage;
import com.fancier.picture.backend.websocket.model.PictureEditResponseMessage;
import com.lmax.disruptor.WorkHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Component
@RequiredArgsConstructor
public class PictureEditEventWorkHandler implements WorkHandler<PictureEditEvent> {

    private final PictureEditHandler pictureEditHandler;

    public void onEvent(PictureEditEvent event) throws Exception {
        PictureEditRequestMessage requestMessage = event.getPictureEditRequestMessage();
        String editAction = requestMessage.getEditAction();
        PictureEditMessageTypeEnum pictureEditMessageTypeEnum = PictureEditMessageTypeEnum.of(editAction);

        LoginUserVO user = event.getUser();
        Long pictureId = event.getPictureId();
        WebSocketSession session = event.getSession();


        switch (pictureEditMessageTypeEnum) {
            case ENTER_EDIT:
                pictureEditHandler.handleEnterEditMessage(user, pictureId);
                break;
            case EDIT_ACTION:
                pictureEditHandler.handleEditActionMessage(user, pictureId, requestMessage.getEditAction(), session);
                break;
            case EXIT_EDIT:
                pictureEditHandler.handleExitEditMessage(user, pictureId);
                break;
            default:
                PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
                responseMessage.setEditAction(PictureEditMessageTypeEnum.ERROR.getText());
                responseMessage.setMessage("消息类型错误");
                responseMessage.setUser(user);
                session.sendMessage(new TextMessage(JSONUtil.toJsonStr(responseMessage)));
        }
    }


}
