package com.example.storage.service;

import com.example.storage.mapper.StorageMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StorageServiceTest {

    @Resource
    private StorageService storageService;

    @Test
    void deduct() {
        storageService.deduct("101010",1);
    }
}