package com.example.storage.controller;

import com.example.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/storage")
public class StorageController {

    @Autowired
    private StorageService storageService;

    @RequestMapping("/deduct")
    public void deduct(@RequestParam String commodityCode,
                       @RequestParam Integer count) {
        storageService.deduct(commodityCode, count);
    }
}
