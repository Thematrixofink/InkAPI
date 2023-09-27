package com.inkslab.openAPI.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.inkslab.openAPI.model.dto.InterfaceInfo.InterfaceInfoQueryRequest;
import com.inkslab.openAPI.model.entity.InterfaceInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 24957
* @description 针对表【interface_info(接口信息)】的数据库操作Service
* @createDate 2023-09-25 16:09:29
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {

    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean b);

    QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest);

}
