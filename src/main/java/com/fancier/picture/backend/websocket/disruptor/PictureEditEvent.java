package com.fancier.picture.backend.websocket.disruptor;

import com.fancier.picture.backend.model.user.vo.UserVO;
import com.fancier.picture.backend.websocket.model.PictureEditRequestMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PictureEditEvent {

    private PictureEditRequestMessage pictureEditRequestMessage;

    private WebSocketSession session;

    /**
     * 当前用户
     */
    private UserVO user;

    private Long pictureId;

}
