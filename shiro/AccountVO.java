package com.gjl.blog.shiro;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class AccountVO implements Serializable {

    private Long id;

    private String username;

    private String avatar;

    private String email;

}
