package com.example.storage.service.impl;

import com.example.storage.entity.Storage;
import com.example.storage.mapper.StorageMapper;
import com.example.storage.service.StorageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class StorageServiceImpl extends ServiceImpl<StorageMapper, Storage> implements StorageService {

    @Resource
    private StorageMapper storageMapper;

    @Override
    public void deduct(String commodityCode, int count) {
        storageMapper.deduct(commodityCode,count);
    }
}
