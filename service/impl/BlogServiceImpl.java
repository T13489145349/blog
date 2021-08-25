package com.gjl.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjl.blog.dao.BlogDAO;
import com.gjl.blog.entity.Blog;
import com.gjl.blog.service.BlogService;
import org.springframework.stereotype.Service;

@Service
public class BlogServiceImpl extends ServiceImpl<BlogDAO, Blog> implements BlogService {
}
