package com.gjl.blog.common.lang;

import lombok.Data;


@Data
public class Result {

    private Integer code;
    private String msg;
    private Object data;


    public static Result success(Object data){
        Result result = new Result();
        result.setCode(200);
        result.setMsg("success");
        result.setData(data);
        return  result;
    }
    public static Result success(String msg,Object data){
        Result result = new Result();
        result.setCode(200);
        result.setMsg(msg);
        result.setData(data);
        return  result;
    }
    public static Result fail(String msg){
        Result result = new Result();
        result.setCode(400);
        result.setMsg(msg);
        result.setData(null);
        return  result;
    }
    public static Result fail(Integer code ,String msg){
        Result result = new Result();
        result.setCode(code);
        result.setMsg(msg);
        return  result;
    }

}
