package com.inkslab.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.inkslab.springbootinit.model.entity.InterfaceInfo;
import com.inkslab.springbootinit.service.InterfaceInfoService;
import com.inkslab.springbootinit.mapper.InterfaceInfoMapper;
import org.springframework.stereotype.Service;

/**
* @author 24957
* @description 针对表【interface_info(接口信息)】的数据库操作Service实现
* @createDate 2023-09-25 16:09:29
*/
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
    implements InterfaceInfoService{

}




