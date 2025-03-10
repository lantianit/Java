package com.example.storage.mapper;

import com.example.storage.entity.Storage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface StorageMapper extends BaseMapper<Storage> {
    @Select("update `storage` set `count`=(`count`-#{count})" +
            " where commodity_code=#{commodityCode}")
    void deduct(@Param("commodityCode") String commodityCode,
                @Param("count") int count);

    @Select("update `storage` set `count`=(`count`+#{count})" +
            " where commodity_code=#{commodityCode}")
    void recover(@Param("commodityCode") String commodityCode,
                @Param("count") int count);
}
