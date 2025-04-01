package com.fancier.picture.backend.websocket;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.fancier.picture.backend.auth.constant.SpacePermission;
import com.fancier.picture.backend.auth.model.SpaceUserAuth;
import com.fancier.picture.backend.model.picture.Picture;
import com.fancier.picture.backend.model.space.Space;
import com.fancier.picture.backend.model.space.constant.SpaceTypeEnum;
import com.fancier.picture.backend.model.user.vo.UserVO;
import com.fancier.picture.backend.service.PictureService;
import com.fancier.picture.backend.service.SpaceService;
import com.fancier.picture.backend.service.SpaceUserService;
import com.fancier.picture.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class WsHandshakeInterceptor implements HandshakeInterceptor {


    private final UserService userService;

    private final PictureService pictureService;

    private final SpaceService spaceService;

    private final SpaceUserService spaceUserService;

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) {


        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest httpServletRequest = ((ServletServerHttpRequest) request).getServletRequest();

            String pictureId = httpServletRequest.getParameter("pictureId");
            if (StrUtil.isBlank(pictureId)) {
                return false;
            }

            UserVO loginUser = userService.getLoginUser();
            if (ObjectUtil.isEmpty(loginUser)) {
                return false;
            }

            Picture picture = pictureService.getById(pictureId);
            if (ObjectUtil.isEmpty(picture)) {
                return false;
            }

            Long spaceId = picture.getSpaceId();
            Space space;
            if (spaceId != null) {
                space = spaceService.getById(spaceId);
                if (!Objects.equals(space.getSpaceType(), SpaceTypeEnum.TEAM.getValue())) {
                    return false;
                }
            }

            String spaceRole = spaceUserService.getSpaceRole(loginUser.getId(), spaceId);
            List<String> permissionList = SpaceUserAuth.getPermissionsByRole(spaceRole);

            if (!permissionList.contains(SpacePermission.PICTURE_EDIT)){
                return false;
            }

            attributes.put("user", loginUser);
            attributes.put("userId", loginUser.getId());
            attributes.put("pictureId", Long.valueOf(pictureId));
        }

        return true;
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, Exception exception) {

    }
}