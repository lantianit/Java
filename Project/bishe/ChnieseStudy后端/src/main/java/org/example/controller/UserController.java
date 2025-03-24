package org.example.controller;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.config.AppVariable;
import org.example.entity.R;
import org.example.entity.User;
import org.example.service.UserService;
import org.example.util.MyJWTUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Value("${images.url}")
    private String imagesUrl;

    // 登录接口
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        log.info("user" + user.toString());
        boolean result = userService.login(user.getLoginName(), user.getPassword());
        if (result) {
            User userByLoginName = userService.getUserByLoginName(user);
            int userId = userByLoginName.getUserId();
            long expirationTime = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", userId);
            payload.put("exp", expirationTime);
            String token = JWTUtil.createToken(payload, "your_secret_key".getBytes());
            JWT jwt = JWTUtil.parseToken(token);
            log.info(String.valueOf(jwt.getPayload("userId")));
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("code", 200);
            responseData.put("message", "登录成功");
            responseData.put("token", token);
            return ResponseEntity.ok().body(responseData);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResultData(401, "用户名或密码错误"));
    }

    // 用于封装返回结果的内部类，可根据实际需求完善更多属性等
    private static class ResultData {
        private int code;
        private String message;

        public ResultData(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    class RegisterRequest {
        private User user;
        private MultipartFile file;

        // 添加无参构造函数
        public RegisterRequest() {
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public MultipartFile getFile() {
            return file;
        }

        public void setFile(MultipartFile file) {
            this.file = file;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("RegisterRequest{");
            sb.append("user=").append(user!= null? user.toString() : "null");
            sb.append(", file=").append(file!= null? file.getOriginalFilename() : "null");
            sb.append('}');
            return sb.toString();
        }

    }

    @PostMapping("/register")
    public ResponseEntity<?> register(User user,@RequestParam("file") MultipartFile avatarFile) {
        if (avatarFile.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("message", "请选择要上传的头像文件");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        try {
            // 获取原始文件名
            String originalFileName = avatarFile.getOriginalFilename();
            // 获取文件后缀名
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));
            // 生成唯一的文件名（使用UUID），避免文件名重复
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            // 构建完整的保存文件路径
            Path filePath = Paths.get(imagesUrl+"\\userphoto\\", uniqueFileName);
            // 创建目标文件对象
            File dest = new File(filePath.toString());
            // 确保目录存在，如果不存在则创建
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            // 将上传的文件内容复制到目标文件
            Files.copy(avatarFile.getInputStream(), filePath);

            // 设置用户头像路径到User对象中
            user.setUserPic(uniqueFileName);

            // 调用MyBatisPlus的service层方法保存用户信息到数据库
            boolean saveResult = userService.save(user);
            if (saveResult) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("code", 200);
                responseData.put("message", "注册成功");
                return ResponseEntity.ok().body(responseData);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
                errorResponse.put("message", "注册失败，保存用户信息到数据库出错");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("message", "图片上传或保存用户信息失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/getUserById")
    public R getUserById(int userId) {
        User user = userService.getById(userId);
        Map<String, Object> map = new HashMap<>();
        map.put("message", user);
        return R.ok(map);
    }

    @GetMapping("/getUserIdByToken")
    public Long getUserIdByToken(String token) {
        log.info("token:{}", token);
        return MyJWTUtils.getUidByToken(token);
    }

    @GetMapping("/getUserByToken")
    public R getUserByToken(String token) {
        log.info("token:{}", token);
        Long uidByToken = MyJWTUtils.getUidByToken(token);
        log.info("uidByToken:{}", uidByToken);
        User user = userService.getById(Integer.parseInt(uidByToken.toString()));
        log.info("根据token获取到的user:{}", user);
        Map<String, Object> map = new HashMap<>();
        map.put("message", user);
        return R.ok(map);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("message", "请选择要上传的文件");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        try {
            // 获取原始文件名
            String originalFileName = file.getOriginalFilename();
            // 获取文件后缀名
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));
            // 生成唯一的文件名（使用UUID），避免文件名重复
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            // 构建完整的保存文件路径
            Path filePath = Paths.get(imagesUrl, uniqueFileName);
            // 创建目标文件对象
            File dest = new File(filePath.toString());
            // 确保目录存在，如果不存在则创建
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            // 将上传的文件内容复制到目标文件
            Files.copy(file.getInputStream(), filePath);

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("code", HttpStatus.OK.value());
            successResponse.put("message", "图片上传成功");
            return ResponseEntity.ok(successResponse);
        } catch (IOException e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("message", "图片上传失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/rank")
    public List<User> getUserScoreRank() {
        return userService.getUserScoreRankList();
    }

}