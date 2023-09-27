package com.inkslab.openAPI.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.inkslab.openAPI.common.ErrorCode;
import com.inkslab.openAPI.constant.CommonConstant;
import com.inkslab.openAPI.exception.BusinessException;
import com.inkslab.openAPI.exception.ThrowUtils;
import com.inkslab.openAPI.model.dto.InterfaceInfo.InterfaceInfoQueryRequest;
import com.inkslab.openAPI.model.entity.*;
import com.inkslab.openAPI.service.InterfaceInfoService;
import com.inkslab.openAPI.mapper.InterfaceInfoMapper;
import com.inkslab.openAPI.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
* @author 24957
* @description 针对表【interface_info(接口信息)】的数据库操作Service实现
* @createDate 2023-09-25 16:09:29
*/
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
    implements InterfaceInfoService{

    /**
     * 参数校验
     * @param interfaceInfo
     * @param add
     */
    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name = interfaceInfo.getName();
        String description = interfaceInfo.getDescription();
        String url = interfaceInfo.getUrl();
        String requestParams = interfaceInfo.getRequestParams();
        String requestHeader = interfaceInfo.getRequestHeader();
        String responseHeader = interfaceInfo.getResponseHeader();
        String method = interfaceInfo.getMethod();

        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(
                    name, description, url,requestHeader,requestParams,responseHeader,method),
                    ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(name) && name.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口名称过长");
        }
        if (StringUtils.isNotBlank(description) && description.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口描述过长");
        }
        if (StringUtils.isNotBlank(url) && url.length() > 200) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "url过长");
        }
        if (StringUtils.isNotBlank(method) && method.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求方式过长");
        }
    }

    @Override
    public QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        if (interfaceInfoQueryRequest == null) {
            return queryWrapper;
        }
        Long id = interfaceInfoQueryRequest.getId();
        String name = interfaceInfoQueryRequest.getName();
        String description = interfaceInfoQueryRequest.getDescription();
        String url = interfaceInfoQueryRequest.getUrl();
        String requestParams = interfaceInfoQueryRequest.getRequestParams();
        String requestHeader = interfaceInfoQueryRequest.getRequestHeader();
        String responseHeader = interfaceInfoQueryRequest.getResponseHeader();
        Integer status = interfaceInfoQueryRequest.getStatus();
        String method = interfaceInfoQueryRequest.getMethod();
        Long userId = interfaceInfoQueryRequest.getUserId();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();

        // 拼接查询条件
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like("name", name);
        }
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        queryWrapper.like(StringUtils.isNotBlank(url), "url", url);
        queryWrapper.like(StringUtils.isNotBlank(requestParams), "requestParams", requestParams);
        queryWrapper.like(StringUtils.isNotBlank(requestHeader), "requestHeader", requestHeader);
        queryWrapper.like(StringUtils.isNotBlank(responseHeader), "responseHeader", responseHeader);

        queryWrapper.ne(ObjectUtils.isNotEmpty(status), "status", status);
        queryWrapper.eq(ObjectUtils.isNotEmpty(method), "method", method);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


}




