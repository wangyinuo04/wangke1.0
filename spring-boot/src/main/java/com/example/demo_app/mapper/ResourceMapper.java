package com.example.demo_app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo_app.entity.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {

    @Select("SELECT r.*, c.chapter_name " +
            "FROM Resource r " +
            "LEFT JOIN Chapter c ON r.chapter_id = c.chapter_id " +
            "WHERE r.chapter_id = #{chapterId} " +
            "ORDER BY r.upload_time DESC")
    List<Resource> selectResourcesWithChapterName(@Param("chapterId") String chapterId);

    @Select("SELECT COUNT(*) FROM Resource WHERE chapter_id = #{chapterId}")
    int countByChapterId(@Param("chapterId") String chapterId);
}