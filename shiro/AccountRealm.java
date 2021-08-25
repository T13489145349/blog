package com.gjl.blog.shiro;

import com.gjl.blog.entity.User;
import com.gjl.blog.service.UserService;
import com.gjl.blog.util.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@Component
public class AccountRealm extends AuthorizingRealm {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    /**
     * 如果token是jwtToken的话，才返回真
     * @param token
     * @return
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return null;
    }

    /**
     * 接收到过滤器中传来的token，对token进行校验
     * @param token
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        JwtToken jwtToken = (JwtToken) token;

        System.out.println("认证token:"+jwtToken);
        String userId = jwtUtils.getClaimByToken((String) jwtToken.getPrincipal()).getSubject();
        System.out.println("认证userid:"+userId);
        User user = userService.getById(userId);
        if(ObjectUtils.isEmpty(user)){
            throw new UnknownAccountException("账户不存在");
        }
        if(user.getStatus() == -1){
            throw new LockedAccountException("账户已被锁定");
        }
        AccountVO accountVO = new AccountVO();
        BeanUtils.copyProperties(user,accountVO);

        return new SimpleAuthenticationInfo(accountVO,jwtToken.getCredentials(),getName());
    }
}
