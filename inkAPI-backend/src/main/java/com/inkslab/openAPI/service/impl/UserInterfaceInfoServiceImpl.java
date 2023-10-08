package com.inkslab.openAPI.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.inkslab.openAPI.common.ErrorCode;
import com.inkslab.openAPI.exception.BusinessException;
import com.inkslab.openAPI.exception.ThrowUtils;
import com.inkslab.openAPI.model.entity.InterfaceInfo;
import com.inkslab.openAPI.model.entity.UserInterfaceInfo;
import com.inkslab.openAPI.service.UserInterfaceInfoService;
import com.inkslab.openAPI.mapper.UserInterfaceInfoMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author 24957
 * @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service实现
 * @createDate 2023-10-07 18:46:58
 */
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
        implements UserInterfaceInfoService {

    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long userId = userInterfaceInfo.getUserId();
        Long interfaceInfoId = userInterfaceInfo.getInterfaceInfoId();
        Integer leftNum = userInterfaceInfo.getLeftNum();


        // 创建时，参数不能为空
        if (add) {
            if (userId <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "非法用户ID");
            }
            if (interfaceInfoId <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "非法接口ID");
            }
            if (leftNum < 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "剩余调用次数不能小于0");
            }
        }
        if (leftNum < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "剩余调用次数不能小于0");
        }
    }

    @Override
    public boolean invokeCount(long interfaceId, long userId) {
        if(interfaceId <= 0 || userId <= 0 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LambdaUpdateWrapper<UserInterfaceInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserInterfaceInfo::getInterfaceInfoId,interfaceId);
        updateWrapper.eq(UserInterfaceInfo::getUserId,userId);
        updateWrapper.setSql("leftNum = leftNum - 1,totalNum = totalNum + 1");
        return this.update(updateWrapper);
    }
}




