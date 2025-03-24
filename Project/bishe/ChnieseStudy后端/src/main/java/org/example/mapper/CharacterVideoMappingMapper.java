package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.entity.CharacterVideoMapping;

import java.util.List;

public interface CharacterVideoMappingMapper extends BaseMapper<CharacterVideoMapping> {
    // 你可以在这里添加自定义的查询方法

    @Select("SELECT * FROM character_video_mapping WHERE `character` LIKE CONCAT('%', #{character}, '%')")
    List<CharacterVideoMapping> findByCharacterLike(@Param("character") String character);
} 