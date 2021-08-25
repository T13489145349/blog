package com.gjl.blog.controller;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gjl.blog.common.lang.Result;
import com.gjl.blog.entity.Blog;
import com.gjl.blog.service.BlogService;
import com.gjl.blog.shiro.AccountVO;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;

@RestController
public class BlogController {

    @Autowired
    private BlogService blogService;

    /**
     * 分页查询所有博客，可传递参数显示当前页数
     * @param currentPage
     * @return
     */
    @GetMapping("/blogs")
    public Result list(@RequestParam(defaultValue = "1") Integer currentPage ,
                       @RequestParam(defaultValue = "5") Integer pageSize){

        Page page = new Page<Blog>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);

        QueryWrapper<Blog> blogQueryWrapper = new QueryWrapper<>();
        blogQueryWrapper.orderByDesc("created");

        IPage iPage = blogService.page(page, blogQueryWrapper);
        System.out.println(iPage);

        return Result.success(iPage);


    }

    /**
     * 根据id查看具体的博客信息
     * @param id
     * @return
     */
    @GetMapping("/blog/{id}")
    public Result details(@PathVariable("id") Long id){

        Blog blog = blogService.getById(id);
        Assert.notNull(blog,"无此博客");
        return Result.success(blog);
    }

    /**
     *
     * 编辑或者新增，根据blog中的id来进行判断，有id代表修改，无则新增
     * @param blog
     * @return
     */
    @RequiresAuthentication
    @PostMapping("/blog/edit")
    public Result editOrSave(@Validated @RequestBody Blog blog){
        //定义temp来接收blog
        Blog temp=null;
        //判断有没有id
        if(blog.getId()==null){
            //新增
            //获取shiro的主体，从主体中取出userid
            AccountVO accountVO = (AccountVO) SecurityUtils.getSubject().getPrincipal();
            //设置属性
            temp=new Blog();
            temp.setUserId(accountVO.getId());
            temp.setCreated(LocalDateTime.now());
            temp.setStatus(0);
        }else {
            //修改
            //通过blog中的id查询出原先的数据
            temp=blogService.getById(blog.getId());
            //判断是否具有修改权限
            AccountVO accountVO = (AccountVO) SecurityUtils.getSubject().getPrincipal();

            System.out.println("***************");
            System.out.println("dbUserId："+temp.getUserId());
            System.out.println("blogUserId："+accountVO.getId());
            System.out.println("***************");
            Assert.isTrue(temp.getUserId().equals(accountVO.getId()),"无权编辑");

        }
        //将用户输入的数据复制到temp中,便于统一处理。要排除掉我们已经设置好的数据
        BeanUtils.copyProperties(blog,temp,"id","userId","created","status");

        blogService.saveOrUpdate(temp);

        return Result.success(null);
    }

}
