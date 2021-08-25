package com.gjl.blog.shiro;

import cn.hutool.json.JSONUtil;
import com.gjl.blog.common.lang.Result;
import com.gjl.blog.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtFilter extends AuthenticatingFilter {


    @Autowired
    private JwtUtils jwtUtils;

    /**
     *   帮我们获取主体对象，并执行login方法
     *   所以将request转为HttpServletRequest来获取请求头中的jwt
     *   如果没有值直接返回空，否则将token转换成jwttoken，
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String jwt = httpServletRequest.getHeader("Authorization");
        if(StringUtils.isEmpty(jwt)){
            return null;
        }
        return new JwtToken(jwt);
    }

    /**
     * 校验jwt ，有则判断状态，无则放行通过（表示为游客，登录）
     * 过滤请求
     * 执行login
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest= (HttpServletRequest) request;
        String token = httpServletRequest.getHeader("Authorization");
        //token为空直接放行，可以访问公共资源，受限资源通过注解控制
        if(StringUtils.isEmpty(token)){
            return true;
        }
       //解析token
        Claims claim = jwtUtils.getClaimByToken(token);
        boolean tokenExpired = jwtUtils.isTokenExpired(claim.getExpiration());
        //claim 为空 或者 token过期
        if(claim.isEmpty()||tokenExpired){
            throw new ExpiredCredentialsException("token已失效，请重新登录");
        }
        //验证token是否正确
        return executeLogin(request,response);
    }

    /**
     * 登录异常时进入该方法，直接将捕捉到的异常抛出到前端
     * @param token
     * @param e
     * @param request
     * @param response
     * @return
     */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletResponse servletResponse = (HttpServletResponse) response;
        //捕捉异常
        Throwable throwable = e.getCause() == null ? e : e.getCause();
        //封装报错信息
        Result result = Result.fail(throwable.getMessage());
        //使用hutool的json工具类转换成json
        String jsonStr = JSONUtil.toJsonStr(result);
        try {
            //直接抛到前端
            servletResponse.getWriter().println(jsonStr);
        } catch (IOException ioException) {

        }

        return false;
    }

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
        HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
        // 跨域时会首先发送一个OPTIONS请求，这里我们给OPTIONS请求直接返回正常状态
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(org.springframework.http.HttpStatus.OK.value());
            return false;
        }

         return super.preHandle(request, response);
    }
}
