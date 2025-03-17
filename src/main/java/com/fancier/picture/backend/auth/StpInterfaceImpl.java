package com.fancier.picture.backend.auth;



import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.fancier.picture.backend.model.user.vo.LoginUserVO;
import org.springframework.stereotype.Component;


import java.util.Collections;
import java.util.List;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Component    // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface {

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 本 list 仅做模拟，实际项目中要根据具体业务逻辑来查询权限
        return Collections.emptyList();
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 本 list 仅做模拟，实际项目中要根据具体业务逻辑来查询角色
        LoginUserVO loginUserVO = (LoginUserVO) StpUtil.getSession().getLoginId();
        String userRole = loginUserVO.getUserRole();
        return Collections.singletonList(userRole);
    }

}
