package com.github.misterchangray.service.user.bo;

import com.github.misterchangray.common.NormalResponse;
import com.github.misterchangray.dao.entity.User;
import com.github.misterchangray.service.common.GlobalCacheService;
import com.github.misterchangray.service.user.UserService;
import com.github.misterchangray.service.user.vo.UserSessionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *
 * session管理
 * @author Rui.Zhang/misterchangray@hotmail.com
 * @author Created on 4/29/2018.
 */
@Component
public class UserSessionBo {
    @Autowired
    private GlobalCacheService globalCacheService;
    @Autowired
    private UserService userService;


    /**
     * 初始化session環境
     * 注冊：onLineUsers 到 servletContext
     */
    public void initUserSessionContext() {
        // 从缓存中获取当前所有用户
        if(null == globalCacheService.get("onLineUsers")) {
            Map<String, UserSessionVO> onLineUsers = new HashMap<String, UserSessionVO>();
            globalCacheService.add("onLineUsers", onLineUsers);
        }
    }

    /**
     * 根据用户ID创建session
     * 一个用户只能创建一个session;多次创建会覆盖
     * @param userId
     */
    public UserSessionVO createSession(String userId) {
        if(null == userId) return null;

        NormalResponse response = userService.getById(Integer.parseInt(userId));
        if(false == response.isSuccess()) return null;
        User user = (User) response.getData();

        // 从缓存中获取当前所有用户
        Map<String, UserSessionVO> onLineUsers = (Map<String, UserSessionVO>) globalCacheService.get("onLineUsers");

        //清除上一次登录session
        UserSessionVO sessionVO = onLineUsers.get(userId);
        if(null != sessionVO) this.destroySession(sessionVO.getSession());

        //构造session
        String token = UUID.randomUUID().toString();
        UserSessionVO userSessionVO = new UserSessionVO(token, user.getUsername(), userId, System.currentTimeMillis());



        onLineUsers.put(userId, userSessionVO);
        onLineUsers.put(token, userSessionVO);


        return userSessionVO;
    }

    /**
     * 获取session
     * @param session
     * @return
     */
    public UserSessionVO getSession(String session) {
        // 从缓存中获取当前Session
        Map<String, UserSessionVO> onLineUsers = (Map<String, UserSessionVO>) globalCacheService.get("onLineUsers");
        return onLineUsers.get(session);
    }


    /**
     * 销毁session
     * @param session
     */
    public void destroySession(String session) {
        // 从缓存中获取当前所有用户
        Map<String, UserSessionVO> onLineUsers = (Map<String, UserSessionVO>) globalCacheService.get("onLineUsers");

        if(null == onLineUsers.get(session)) return;
        UserSessionVO userSessionVO = onLineUsers.get(session);
        if(null == userSessionVO) return;


        onLineUsers.remove(userSessionVO.getSession());
        onLineUsers.remove(userSessionVO.getUserId());
    }


    /**
     * 判断session是否存在
     * @param session
     */
    public boolean exist(String session) {
        // 从缓存中获取当前所有用户
        Map<String, UserSessionVO> onLineUsers = (Map<String, UserSessionVO>) globalCacheService.get("onLineUsers");

        if(null == onLineUsers.get(session)) return false;
        return true;
    }


    /**
     * 更新session心跳時間
     * @param session
     */
    public void heartbeat(String session) {
        Map<String, UserSessionVO> onLineUsers = (Map<String, UserSessionVO>) globalCacheService.get("onLineUsers");
        UserSessionVO userSessionVO = onLineUsers.get(session);
        userSessionVO.setHeartBeatDate(System.currentTimeMillis());
    }

}
