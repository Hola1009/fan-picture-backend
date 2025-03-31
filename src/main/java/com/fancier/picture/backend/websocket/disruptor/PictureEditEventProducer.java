package com.fancier.picture.backend.websocket.disruptor;

import com.fancier.picture.backend.model.user.vo.LoginUserVO;
import com.fancier.picture.backend.websocket.model.PictureEditRequestMessage;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * 图片编辑时间生产者
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Component
public class PictureEditEventProducer {
    @Resource
    private Disruptor<PictureEditEvent> pictureEditEventDisruptor;

    public void publishEvent(PictureEditRequestMessage requestMessage, WebSocketSession session, LoginUserVO user, Long pictureId) {
        RingBuffer<PictureEditEvent> ringBuffer = pictureEditEventDisruptor.getRingBuffer();
        // 获取到可以防止事件的位置
        long next = ringBuffer.next();
        PictureEditEvent pictureEditEvent = ringBuffer.get(next);
        pictureEditEvent.setPictureEditRequestMessage(requestMessage);
        pictureEditEvent.setSession(session);
        pictureEditEvent.setUser(user);
        pictureEditEvent.setPictureId(pictureId);
        // 发布事件
        ringBuffer.publish(next);
    }

    /**
     * 优雅停机
     */
    @PreDestroy
    public void destroy() {
        pictureEditEventDisruptor.shutdown();
    }
}
