package org.example.entity.VO;

import lombok.Data;

import java.util.Date;

@Data
public class FavoritedVideoVO {
    private Long videoId;
    private String videoUrl;
    private String title;
    private String description;
    private Date favoritedTime;
} 