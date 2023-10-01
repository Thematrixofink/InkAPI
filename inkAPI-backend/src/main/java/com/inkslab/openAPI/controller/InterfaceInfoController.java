package com.inkslab.openAPI.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.inkslab.inkapiclientsdk.client.inkClient;
import com.inkslab.openAPI.annotation.AuthCheck;
import com.inkslab.openAPI.common.BaseResponse;
import com.inkslab.openAPI.common.DeleteRequest;
import com.inkslab.openAPI.common.ErrorCode;
import com.inkslab.openAPI.common.ResultUtils;
import com.inkslab.openAPI.constant.UserConstant;
import com.inkslab.openAPI.exception.BusinessException;
import com.inkslab.openAPI.exception.ThrowUtils;
import com.inkslab.openAPI.model.dto.InterfaceInfo.*;
import com.inkslab.openAPI.model.entity.InterfaceInfo;
import com.inkslab.openAPI.model.entity.User;
import com.inkslab.openAPI.model.enums.InterfaceInfoStatusEnum;
import com.inkslab.openAPI.service.InterfaceInfoService;
import com.inkslab.openAPI.service.UserService;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.parsson.JsonUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Objects;

@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
public class InterfaceInfoController {


    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;

    @Resource
    private inkClient inkClient;

    /**
     * 创建
     *
     * @param interfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        interfaceInfo.setUserId(loginUser.getId());
        boolean result = interfaceInfoService.save(interfaceInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newInterfaceInfoId = interfaceInfo.getId();
        return ResultUtils.success(newInterfaceInfoId);
    }

    /**
     * 删除
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        InterfaceInfo oldinterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldinterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldinterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = interfaceInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     * @param interfaceInfoUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest) {
        if (interfaceInfoUpdateRequest == null || interfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);
        // 参数校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, false);
        long id = interfaceInfoUpdateRequest.getId();
        // 判断是否存在
        InterfaceInfo oldinterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldinterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<InterfaceInfo> getInterfaceInfoById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(interfaceInfo);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<InterfaceInfo>> listenInterfaceInfoByPage(@RequestParam("current") long current,@RequestParam("pageSize") long size) {
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size));
        return ResultUtils.success(interfaceInfoPage);
    }

    @PostMapping("/online")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> interfaceOnline(@RequestBody IdRequest idRequest, HttpServletRequest request){
        if(idRequest == null || idRequest.getId() <= 0 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = idRequest.getId();
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if(interfaceInfo == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //如果接口的状态已经是上线了，那么就不要执行上面的操作了
        Integer status = interfaceInfo.getStatus();
        if(status == 1){
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        //检测接口是否能够调用
        //todo 根据测试地址调用
        com.inkslab.inkapiclientsdk.model.User user = new com.inkslab.inkapiclientsdk.model.User();
        user.setName("test");
        String nameByPostBody = inkClient.getNameByPostBody(user);
        if(nameByPostBody == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"接口调用失败");
        }
        InterfaceInfo newInterface = new InterfaceInfo();
        newInterface.setId(id);
        newInterface.setStatus(InterfaceInfoStatusEnum.ONLINE.getValue());
        boolean update = interfaceInfoService.updateById(newInterface);
        if(!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(update);
    }

    @PostMapping("/offline")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> interfaceOffine(@RequestBody IdRequest idRequest, HttpServletRequest request){
        if(idRequest == null || idRequest.getId() <= 0 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = idRequest.getId();
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if(interfaceInfo == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //如果接口的状态已经是了，那么就不要执行上面的操作了
        Integer status = interfaceInfo.getStatus();
        if(status == 0){
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        InterfaceInfo newInterface = new InterfaceInfo();
        newInterface.setId(id);
        newInterface.setStatus(InterfaceInfoStatusEnum.OFFLINE.getValue());
        boolean update = interfaceInfoService.updateById(newInterface);
        if(!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(update);
    }

    @PostMapping("/invoke")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Object> invokeInterface(@RequestBody InterfaceInvokeRequest interfaceInvokeRequest,HttpServletRequest request){
        if(interfaceInvokeRequest == null || interfaceInvokeRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = interfaceInvokeRequest.getId();
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if(interfaceInfo == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if(Objects.equals(interfaceInfo.getStatus(), InterfaceInfoStatusEnum.OFFLINE.getValue())){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        User user = userService.getLoginUser(request);
        String accessKey = user.getAccessKey();
        String secretKey = user.getSecretKey();
        inkClient tempClient = new inkClient(accessKey,secretKey);
        //todo 根据不同的地址改为不同的方法
        String nameByPostBody = tempClient.getNameByPostBody(JSONUtil.toBean(interfaceInvokeRequest.getUserRequestParams(), com.inkslab.inkapiclientsdk.model.User.class));
        return ResultUtils.success(nameByPostBody);

    }
}
