package com.tcp;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.enums.ErrorEnum;
import com.exceptions.BusinessException;
import com.utils.LoginHelper;
import com.utils.LoginUser;
import com.utils.RpcResult;
import com.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@SaIgnore
@Validated
public class OAuthController {

    /**
     * 退出登录
     */
    @GetMapping("/logout")
    public RpcResult<String> logout() {
        StpUtil.logout();
        return RpcResult.success("退出成功");
    }

    @GetMapping("/testLogin")
    public RpcResult<String> testLogin() {
        String activeProfile = SpringUtils.getActiveProfile();
     /*   if (!"local".equals(activeProfile) && !"test".equals(activeProfile)) {
            throw new BusinessException(ErrorEnum.AUTH_ERROR, "环境不可用");
        }*/
        // 此处可根据登录用户的数据不同 自行创建 loginUser 属性不够用继承扩展就行了
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(1824351035133280258L);
        loginUser.setUsername("Carl Liu");
        loginUser.setUserType("pc");
        SaLoginModel model = new SaLoginModel();
        // 自定义分配 不同用户体系 不同 token 授权时间 不设置默认走全局 yml 配置
        // 例如: 后台用户30分钟过期 app用户1天过期
        // 生成token
        LoginHelper.login(loginUser, model);

        return RpcResult.success(StpUtil.getTokenValue());
    }
}
