package com.fancier.picture.backend.auth.constant;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
public interface StpKit {
    StpLogic DEFAULT = StpUtil.stpLogic;

    StpLogic USER = new StpLogic("user");

    StpLogic SPACE = new StpLogic("space");

}
