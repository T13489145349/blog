package com.gjl.blog.controller;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gjl.blog.common.dto.LoginDTO;
import com.gjl.blog.common.lang.Result;
import com.gjl.blog.entity.User;
import com.gjl.blog.service.UserService;
import com.gjl.blog.shiro.AccountVO;
import com.gjl.blog.util.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;


    @RequiresAuthentication
    @GetMapping("/{id}")
    public Result testUser(@PathVariable("id") Long id){
        User byId = userService.getById(id);
        System.out.println(byId);
        return Result.success(byId);
    }

    @PostMapping("/save")
    public Result testUser(@Validated @RequestBody User user){
        return Result.success(user);
    }

    @PostMapping("/login")
    public Result login(@Validated @RequestBody LoginDTO loginDTO, HttpServletResponse response){
        //查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",loginDTO.getUsername());
        User user = userService.getOne(queryWrapper);
        //不存在直接抛异常
        Assert.notNull(user,"用户不存在");
        //判断密码
        if(!user.getPassword().equals(SecureUtil.md5(loginDTO.getPassword()))){
            //不正确
            return Result.fail("密码不正确");
        }
        //正确,根据用户id生成token
        String token = jwtUtils.generateToken(user.getId());
        //把token放进响应头，前端从响应头接收
        response.setHeader("Authorization",token);
        response.setHeader("Access-control-Expose-Headers","Authorization");

        AccountVO accountVO = new AccountVO();
        BeanUtils.copyProperties(user,accountVO);
        return Result.success(accountVO);

    }
    @RequiresAuthentication
    @GetMapping("/logout")
    public Result logout(){
        SecurityUtils.getSubject().logout();
        return Result.success(null);
    }

}
