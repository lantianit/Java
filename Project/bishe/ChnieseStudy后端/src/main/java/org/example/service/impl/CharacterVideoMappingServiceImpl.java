package org.example.service.impl;

import org.example.entity.CharacterVideoMapping;
import org.example.mapper.CharacterVideoMappingMapper;
import org.example.service.CharacterVideoMappingService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import javax.annotation.Resource;
import java.util.List;

@Service
public class CharacterVideoMappingServiceImpl implements CharacterVideoMappingService {

    @Resource
    private CharacterVideoMappingMapper characterVideoMappingMapper;

    @Override
    public CharacterVideoMapping getMappingByCharacter(String character) {
        return characterVideoMappingMapper.selectOne(
            new QueryWrapper<CharacterVideoMapping>().eq("character", character)
        );
    }

    @Override
    public boolean save(CharacterVideoMapping mapping) {
        return characterVideoMappingMapper.insert(mapping) > 0;
    }

    @Override
    public List<CharacterVideoMapping> findByCharacterLike(String character) {
        return characterVideoMappingMapper.findByCharacterLike(character);
    }
} 