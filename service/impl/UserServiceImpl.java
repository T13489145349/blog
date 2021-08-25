package com.gjl.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjl.blog.dao.UserDAO;
import com.gjl.blog.entity.User;
import com.gjl.blog.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserDAO, User> implements UserService {
}
