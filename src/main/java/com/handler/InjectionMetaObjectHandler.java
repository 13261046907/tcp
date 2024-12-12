package com.handler;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.enums.ErrorEnum;
import com.exceptions.BusinessException;
import com.model.BaseEntity;
import com.utils.LoginHelper;
import com.utils.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * MP注入处理器
 */
@Slf4j
@Component
public class InjectionMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        try {
            if (ObjectUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof BaseEntity) {
                BaseEntity baseEntity = (BaseEntity) metaObject.getOriginalObject();
                Date current = ObjectUtil.isNotNull(baseEntity.getCreateTime())
                        ? baseEntity.getCreateTime() : new Date();
                baseEntity.setCreateTime(current);
                baseEntity.setUpdateTime(current);
                // TODO: 2024/11/18 官网上限之后替换
                // LoginUser loginUser = getLoginUser();
                // if (ObjectUtil.isNotNull(loginUser)) {
                //     Long userId = ObjectUtil.isNotNull(baseEntity.getCreateBy())
                //             ? baseEntity.getCreateBy() : loginUser.getUserId();
                //     // 当前已登录 且 创建人为空 则填充
                //     baseEntity.setCreateBy(userId);
                //     // 当前已登录 且 更新人为空 则填充
                //     baseEntity.setUpdateBy(userId);
                // }
                if (ObjectUtil.isNotNull(LoginHelper.getUserId())) {
                    Long userId = ObjectUtil.isNotNull(baseEntity.getCreateBy())
                            ? baseEntity.getCreateBy() : LoginHelper.getUserId();
                    // 当前已登录 且 创建人为空 则填充
                    baseEntity.setCreateBy(userId);
                    // 当前已登录 且 更新人为空 则填充
                    baseEntity.setUpdateBy(userId);
                }
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorEnum.AUTH_ERROR, ErrorEnum.AUTH_ERROR.getMessage());
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        try {
            if (ObjectUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof BaseEntity) {
                BaseEntity baseEntity = (BaseEntity) metaObject.getOriginalObject();
                Date current = new Date();
                // 更新时间填充(不管为不为空)
                baseEntity.setUpdateTime(current);
                // LoginUser loginUser = getLoginUser();
                // // 当前已登录 更新人填充(不管为不为空)
                // if (ObjectUtil.isNotNull(loginUser)) {
                //     baseEntity.setUpdateBy(loginUser.getUserId());
                // }
                if (ObjectUtil.isNotNull(LoginHelper.getUserId())) {
                    baseEntity.setUpdateBy(LoginHelper.getUserId());
                }
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorEnum.AUTH_ERROR, ErrorEnum.AUTH_ERROR.getMessage());
        }
    }

    /**
     * 获取登录用户名
     */
    private LoginUser getLoginUser() {
        LoginUser loginUser;
        try {
            loginUser = LoginHelper.getLoginUser();
        } catch (Exception e) {
            // log.warn("自动注入警告 => 用户未登录");
            return null;
        }
        return loginUser;
    }

}
