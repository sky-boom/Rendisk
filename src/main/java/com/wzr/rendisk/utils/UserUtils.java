package com.wzr.rendisk.utils;

import com.wzr.rendisk.config.shiro.JwtRealm;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;

import java.util.UUID;

/**
 * 登录认证工具包
 * @author wzr
 * @date 2023-06-02 19:18
 */
public class UserUtils {
    /** md5 */
    private static final String ENCODE_TYPE_MD5 = "MD5";
    /** hash次数 */
    private static final int HASH_ITERATIONS = 1024;
    
    /** 根据用户名获取对应的 盐 */
    private static Object getSalt(String username) {
        return ByteSource.Util.bytes(username);
    }
    
    /**
     * 生成安全的密码，利用Shiro内置的md5编码方法
     * @return md5加密密码
     */
    public static String encryptPassword(String plainPassword) {
        Object salt = getSalt(plainPassword);
        SimpleHash simpleHash = new SimpleHash(ENCODE_TYPE_MD5, plainPassword, salt, HASH_ITERATIONS);
        return simpleHash.toString();
    }

    /**
     * 验证密码
     * @param plainPassword 明文密码
     * @param password 密文密码
     * @return 验证成功返回true
     */
    public static boolean validatePassword(String plainPassword, String password) {
        return password.equals(encryptPassword(plainPassword));
    }

    /**
     * 获取一个独一无二（maybe）的昵称
     * @return 昵称
     */
    public static String getRandomNickname() {
        String uuid = UUID.randomUUID().toString();
        return "路人_" + uuid.substring(0, 8);
    }
    
}
