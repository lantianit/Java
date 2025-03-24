package org.example.service;

import org.example.entity.CharacterVideoMapping;

import java.util.List;

public interface CharacterVideoMappingService {
    CharacterVideoMapping getMappingByCharacter(String character);
    boolean save(CharacterVideoMapping mapping);
    List<CharacterVideoMapping> findByCharacterLike(String character);
} 