package org.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.CarouselImage;
import org.example.entity.R;
import org.example.service.CarouselImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/carouseImage")
@Slf4j
public class CarouseImageController {

    @Resource
    private CarouselImageService carouseImageService;

    @Value("${images.url}")
    private String imagesUrl;

    @GetMapping("/getCarouseImage")
    public R getCarouseImage() {
        List<CarouselImage> list = carouseImageService.list();
        return R.ok(Map.of("message", list));
    }

    @PostMapping("/addCarouseImage")
    public R addCarouseImage(@RequestBody CarouselImage carouselImage) {
        carouseImageService.save(carouselImage);
        return R.ok();
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("文件为空，请选择一个文件上传。");
        }

        try {
            File uploadDir = new File(imagesUrl);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            Path filePath = Paths.get(imagesUrl, uniqueFileName);
            Files.write(filePath, file.getBytes());
            log.info(" >>> 上传一张图片，图片的url是：" + filePath.toAbsolutePath() + "   >>>");
            return ResponseEntity.ok(filePath.toString());

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("文件上传失败。");
        }
    }
}
