package com.wzr.rendisk.config.shiro;

import com.alibaba.fastjson.JSON;
import com.wzr.rendisk.core.constant.JwtConstant;
import com.wzr.rendisk.core.result.GlobalResult;
import com.wzr.rendisk.core.result.ResultCode;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 用户登录鉴权流程，主要就是在此类实现。
 *  1.判断请求是否需要进行登录认证授权(可在此写拦截白名单)，
 *      如果需要则该请求就必须在Header中添加Authorization字段存放AccessToken，
 *      无需授权即游客直接访问(有权限管控的话，以游客访问就会被拦截)。
 *  2.调用getSubject(request, response).login(token)，
 *      将AccessToken提交给shiro中的UserRealm进行认证。
 * 
 * 注意：在此类中可能需要避免显式抛异常（应该会破坏shiro的过滤器链），
 *  如果想向前端返回消息，使用response设置就好了。
 *      
 * 参考：https://www.doufuplus.com/blog/shiro-jwt03.html
 * 
 * @author wzr
 * @date 2023-06-03 0:12
 */

public class JwtFilter extends BasicHttpAuthenticationFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);
    
    /**
     * 程序的入口。判断当前方法是否需要进行认证。
     * 游客和已登录用户的区别在于，游客请求头不含有jwt。
     * 对于游客或者认证失败的用户，会阻止当前请求，调用onLoginFail方法返回失败信息。
     * @param request
     * @param response
     * @param mappedValue
     * @return
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        // 检查请求头是否包含jwt。如果有，那么需要认证。
        if (this.isLoginAttempt(request, response)) {
            try {
                // 只有这里才可能会返回true
                return executeLogin(request, response);
            } catch (AuthenticationException | JwtException e) {
                // 认证的过程中，只要出现一个异常，就会返回错误信息。
                e.printStackTrace();
                this.onLoginFail(response, ResultCode.NEED_TO_LOGIN);
                return false;
            } catch (Exception e) {
                // 非认证流程出现的异常
                e.printStackTrace();
                this.onLoginFail(response, ResultCode.ERROR);
                return false;
            }
        }
        // 如果请求头不带有jwt，说明可能是游客，提示接口无权访问，需登录。
        this.onLoginFail(response, ResultCode.GUEST_NEED_TO_LOGIN);
        return false;
    }
    
    /**
     * 当前请求是否属于尝试登录？此方法由 onAccessDenied() 调用。
     *  1. Shiro默认实现是：看看当前请求是否带有相关的请求头信息。
     *  2. 自定义默认实现：看看当前请求是否带有jwt相关的请求头信息。
     *      如果没有带有jwt，则说明当前请求应该是登录请求（不排除有非法请求）
     * @param request request
     * @param response response
     * @return 是否属于尝试登录(认证)
     */
    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        String authorization = req.getHeader(JwtConstant.JWT_HEADER_NAME);
        return StringUtils.isNotEmpty(authorization);
    }

    /**
     * 执行登录，
     * 使用subject.login(token)方法，把jwt当作参数传入。
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        // 拿到请求头的jwt
        HttpServletRequest req = (HttpServletRequest) request;
        String authorization = req.getHeader(JwtConstant.JWT_HEADER_NAME);
        // 把jwt传给Realm进行认证，如果抛异常，会被isAccessAllowed()方法的catch代码块捕获。
        this.getSubject(request, response).login(new TokenModel(authorization));
        // 如果没有异常，说明认证成功。
        return true;
    }


    /**
     *  isAccessAllowed()返回false时，才会执行当前方法。
     *  为了防止Realm重复认证，重写该类方法，以防止再次调用父类的executeLogin()方法。
     *  
     *  Shiro默认实现是：
     *      调用isLoginAttempt()方法（判断用户是否尝试登录）。
     *      如果是，则调用executeLogin()方法（执行登录）。
     *          如果登录失败（executeLogin()返回false），则设置相应请求头，返回false
     *          如果登录成功，返回true
     *      如果不是，返回false即可。
     * @param request request
     * @param response response
     * @return true: 请求允许放行。false: 不允许放行。
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        return false;
    }
    
    /**
     * 对跨域提供支持
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers",
                httpServletRequest.getHeader("Access-Control-Request-Headers"));
        // 跨域时会首先发送一个OPTIONS请求，这里我们给OPTIONS请求直接返回正常状态
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }

    /**
     * 将非法请求跳转
     */
    private void onLoginFail(ServletResponse response, ResultCode resultCode) {
        try {
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setContentType("application/json");
            httpServletResponse.setCharacterEncoding("UTF-8");
            // 返回错误消息
            byte[] bytes = JSON.toJSONString(GlobalResult.error(resultCode)).getBytes();
            httpServletResponse.getOutputStream().write(bytes);
        } catch (IOException e) {
            // 错误日志
            log.error(e.getMessage());
        }
    }
}
