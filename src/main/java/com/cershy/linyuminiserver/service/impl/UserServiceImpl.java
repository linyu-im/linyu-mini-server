package com.cershy.linyuminiserver.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cershy.linyuminiserver.constant.NotifyType;
import com.cershy.linyuminiserver.dto.NotifyDto;
import com.cershy.linyuminiserver.dto.UserDto;
import com.cershy.linyuminiserver.entity.User;
import com.cershy.linyuminiserver.mapper.UserMapper;
import com.cershy.linyuminiserver.service.UserService;
import com.cershy.linyuminiserver.service.WebSocketService;
import com.cershy.linyuminiserver.vo.user.CreateUserVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    UserMapper userMapper;

    @Resource
    WebSocketService webSocketService;

    @Override
    public boolean isExist(String name, String email) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getName, name)
                .or().eq(User::getEmail, email);
        return count(queryWrapper) > 0;
    }

    @Override
    public User getUserByNameOrEmail(String name, String email) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getName, name)
                .or().eq(User::getEmail, email);
        return getOne(queryWrapper);
    }

    @Override
    public User createUser(CreateUserVo createUserVo) {
        User user = new User();
        user.setId(IdUtil.simpleUUID());
        user.setName(createUserVo.getName());
        user.setEmail(createUserVo.getEmail());
        save(user);
        return user;
    }

    @Override
    public UserDto getUserById(String userId) {
        return userMapper.getUserById(userId);
    }

    @Override
    public List<UserDto> listUser() {
        return userMapper.listUser();
    }

    @Override
    public List<String> onlineWeb() {
        return webSocketService.getOnlineUser();
    }

    @Override
    public Map<String, UserDto> listMapUser() {
        return userMapper.listMapUser();
    }

    @Override
    public void online(String userId) {
        NotifyDto notifyDto = new NotifyDto();
        notifyDto.setTime(new Date());
        notifyDto.setType(NotifyType.Web_Online);
        notifyDto.setContent(JSONUtil.toJsonStr(getUserById(userId)));
        webSocketService.sendNotifyToGroup(notifyDto);
    }

    @Override
    public void offline(String userId) {
        NotifyDto notifyDto = new NotifyDto();
        notifyDto.setTime(new Date());
        notifyDto.setType(NotifyType.Web_Offline);
        notifyDto.setContent(JSONUtil.toJsonStr(getUserById(userId)));
        webSocketService.sendNotifyToGroup(notifyDto);
    }
}
