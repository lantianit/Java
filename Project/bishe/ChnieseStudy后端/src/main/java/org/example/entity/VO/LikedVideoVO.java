package org.example.entity.VO;

import lombok.Data;

import java.util.Date;

@Data
public class LikedVideoVO {
    private Long videoId;
    private String videoUrl;
    private String title;
    private String description;
    private Date likedTime;
} 