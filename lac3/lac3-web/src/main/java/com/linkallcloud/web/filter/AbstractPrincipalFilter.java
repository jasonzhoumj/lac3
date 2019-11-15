package com.linkallcloud.web.filter;

import com.linkallcloud.core.dto.Result;
import com.linkallcloud.core.exception.BizException;
import com.linkallcloud.core.lang.Strings;
import com.linkallcloud.core.principal.Assertion;
import com.linkallcloud.core.principal.Principal;
import com.linkallcloud.core.www.utils.WebUtils;
import com.linkallcloud.web.session.SessionUser;
import com.linkallcloud.web.utils.Controllers;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


public abstract class AbstractPrincipalFilter extends LacCommonFilter {

    public AbstractPrincipalFilter() {
        super();
    }

    public AbstractPrincipalFilter(List<String> ignoreRes, boolean override) {
        super(ignoreRes, override);
    }

    @Override
    protected void doConcreteFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        SessionUser u = getLoginUser(getAppCode(), request);
        if (null == u) { // suser为空，表示初次登陆或者本地session超时
            // 若有sso, 并且sso未超时, principal为sso验证后的用户帐号
            Principal principal = getSSOPrincipal(request);
            if (principal != null) {// SSO认证，且已经通过SSO认证，但是本地未登陆
                SessionUser user = getSessionUserByPrincipal(principal);
                if (null != user) {
                    loginUser(request, getAppCode(), user);
                } else {
                    throw new BizException("AbstractPrincipalFilter", "登录失败.");
                }
            } else if (isIndexOrLocalLoginRequest(request)) {// 不是SSO认证，且是访问首页(登陆页面)
                chain.doFilter(request, response);
                return;
            } else {
                gotoLogin(getLoginUrl(), request, response);
                return;
            }
        } else {
            String currentAppCode = Controllers.getCurrentAppKey();
            String thisAppCode = getAppCode();
            if (Strings.isBlank(currentAppCode) || !currentAppCode.equals(thisAppCode)) {
                Controllers.switchLogin2App(thisAppCode);
            }
        }

        chain.doFilter(request, response);
    }


    protected abstract String getAppCode();

    protected abstract String getLoginUrl();

    /**
     * 得到SSO认证通过后设置的Principal。
     *
     * @param request
     * @return Principal
     */
    private Principal getSSOPrincipal(HttpServletRequest request) {
        Assertion as = (Assertion) Controllers.getSessionObject(Assertion.ASSERTION_KEY);
        if (as != null) {
            return as.getPrincipal();
        }
        return null;
    }

    /**
     * 是否系统首页或者是本地登录的请求，用于非SSO认证过滤时的判断
     *
     * @param request
     * @return boolean
     */
    protected boolean isIndexOrLocalLoginRequest(HttpServletRequest request) {
        String sp = request.getServletPath();
        if (sp.indexOf("/index.html") != -1 || sp.indexOf("/login") != -1) {
            return true;
        }
        return false;
    }

    /**
     * 根据ssoPrincipal加载用户Dto，并转化成SessionUser
     *
     * @param ssoPrincipal
     * @return SessionUser
     */
    protected abstract SessionUser getSessionUserByPrincipal(Principal ssoPrincipal);

    /**
     * 把用户信息放入session(cookie)中，可以覆盖此方法，在session(cookie)中放入多需要的信息，比如权限
     *
     * @param request
     * @param appCode
     * @param user
     */
    protected void loginUser(HttpServletRequest request, String appCode, SessionUser user) {
        Controllers.login(appCode, user);
    }

    /**
     * @param loginUrl
     * @param request
     * @param hResponse
     * @throws IOException
     */
    protected void gotoLogin(String loginUrl, HttpServletRequest request, HttpServletResponse hResponse) throws IOException {
        if (WebUtils.isAjax(request)) {
            Result<String> result = new Result<>(loginUrl);
            WebUtils.out(hResponse, result);
        } else {
            hResponse.sendRedirect(loginUrl);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig arg0) throws ServletException {
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
    }

}