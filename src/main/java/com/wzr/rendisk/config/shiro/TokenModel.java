package com.wzr.rendisk.config.shiro;

import lombok.Data;
import org.apache.shiro.authc.AuthenticationToken;

/**
 * jwt的身份信息，交给Shiro认证
 * @author wzr
 * @date 2023-06-04 19:29
 */
@Data
public class TokenModel implements AuthenticationToken {

    /** jwtToken */
    private String jwtToken; 
    
    public TokenModel(String jwtToken) {
        this.jwtToken = jwtToken;
    }
    
    @Override
    public Object getPrincipal() {
        return this.jwtToken;
    }

    @Override
    public Object getCredentials() {
        return this.jwtToken;
    }
}
