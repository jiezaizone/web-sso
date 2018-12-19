package com.isuwang.web.sso.open;

import com.isuwang.web.sso.comm.AbstractRestController;
import com.isuwang.web.sso.comm.RestResponse;
import com.isuwang.web.sso.comm.utils.*;
import com.isuwang.web.sso.config.redis.RedisCacheService;
import com.isuwang.web.sso.dbmapper.UserMapper;
import com.isuwang.web.sso.dbmodel.User;
import com.isuwang.web.sso.dbmodel.UserExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangxibin on 2017/8/28.
 */
@RestController
@RequestMapping()
public class SsoController extends AbstractRestController{
    @Autowired
    private RedisCacheService redisCacheService;
    @Autowired
    UserMapper userMapper;

    /**
     * 登录接口
     * @param username 用户名
     * @param password 密码
     * @return
     */
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public RestResponse login(String username, String password){
        UserExample example =new UserExample();
        example.createCriteria().andUsernameEqualTo(username);
        List<User> list = userMapper.selectByExample(example);
        if(1 == list.size()){
            User user = list.get(0);
            String pm = PasswordUtil.springSecurityPasswordEncode(password, username);
            if (pm.equals(user.getPassword())) {
                //保存到redis,根据凭证找用户
                Long cur = System.currentTimeMillis();
                cur+=30*60*1000;
                String token = "token_api_"+ UUIDGenerator.uuid();
                redisCacheService.set(token,username,30*60);//使用token为key，存用户名 超过30*60秒自动删除
                redisCacheService.set(token+"_time",cur.toString(),30*60);
                redisCacheService.setObject(username,user,30*60);//使用用户名为key存用户信息
                //请随便改造吧。返回内容
                Map map = new HashMap<>();
                map.put("token",token);
                return RestResponse.success(map);
            }
        }
        return RestResponse.failed("0020","用户名或密码不正确");
    }


    /**
     * 新增用户
     * @param username 用户名
     * @param passwd 密码
     * @return
     */
    @RequestMapping(value = "/add",method = RequestMethod.POST)
    public RestResponse add( String username, String passwd){
        User user = new User();
        user.setId(UUIDGenerator.uuid());
        user.setUsername(username);
        user.setPassword(PasswordUtil.springSecurityPasswordEncode(passwd, username));
        int re = userMapper.insert(user);
        return RestResponse.success(re);
    }
    /**
     * 登出接口
     * @return
     */
    @RequestMapping(value = "/to/logout",method = RequestMethod.GET)
    public RestResponse toLogout(){
        redisCacheService.del(getCurrentAccountUserName());
        return RestResponse.success("登出成功");
    }
    @RequestMapping(value = "/logout/{token}",method = RequestMethod.GET)
    public RestResponse logout(@PathVariable String token){
        String username = redisCacheService.get(token);
        if(StringUtil.isNotBlank(username)){
            redisCacheService.del(username);
            redisCacheService.del(token);
            redisCacheService.del(token+"_time");
        }
        return RestResponse.success("登出成功");
    }

    /**
     * 根据token获取用户信息
     * @param token
     * @return
     */
    @RequestMapping(value = "/cur/user/{token}",method = RequestMethod.GET)
    public RestResponse getCurUser(@PathVariable String token){
        String username = redisCacheService.get(token);
        if(StringUtil.isNotBlank(username)){
            String str = redisCacheService.get(username);
            User user = JSONUtil2.fromJson(str,User.class);
            return RestResponse.success(user);
        }else{
            return RestResponse.failed("0000","token不存在！");
        }

    }

    //检查token的有效性
    @RequestMapping(value = "/check/token/{token}",method = RequestMethod.GET)
    public RestResponse checkToken(@PathVariable String token){
        String username = redisCacheService.get(token);
        if(StringUtil.isNotBlank(username)){
            return RestResponse.success("当前token在有效期内");
        }else{
            return RestResponse.failed("0000","token不存在！");
        }
    }
    //刷新token
    @RequestMapping(value = "/refresh/token/{token}",method = RequestMethod.GET)
    public RestResponse refreshToken(@PathVariable String token){
        String username = redisCacheService.get(token);
        if(StringUtil.isNotBlank(username)){
            Long cur = System.currentTimeMillis();
            cur+=30*60*1000;

            redisCacheService.expire(token,30*60);
            redisCacheService.expire(token+"_time",30*60);
            redisCacheService.expire(username,30*60);

            Map map = new HashMap();
            map.put("time",cur);
            return RestResponse.success(map);
        }else{
            return RestResponse.failed("0000","token不存在！");
        }
    }

}
