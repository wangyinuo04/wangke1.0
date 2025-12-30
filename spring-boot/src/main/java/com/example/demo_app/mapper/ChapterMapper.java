package com.example.demo_app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo_app.entity.Chapter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChapterMapper extends BaseMapper<Chapter> {

    @Select("SELECT c.*, p.chapter_name as parent_chapter_name " +
            "FROM Chapter c " +
            "LEFT JOIN Chapter p ON c.parent_chapter_id = p.chapter_id " +
            "WHERE c.class_id = #{classId} " +
            "ORDER BY c.parent_chapter_id, c.sort_order, c.chapter_name")
    List<Chapter> selectChaptersWithParentName(@Param("classId") String classId);

    @Select("SELECT * FROM Chapter WHERE class_id = #{classId} AND parent_chapter_id IS NULL " +
            "ORDER BY sort_order, chapter_name")
    List<Chapter> selectRootChapters(@Param("classId") String classId);

    @Select("SELECT * FROM Chapter WHERE class_id = #{classId} AND parent_chapter_id = #{parentId} " +
            "ORDER BY sort_order, chapter_name")
    List<Chapter> selectChildChapters(@Param("classId") String classId, @Param("parentId") String parentId);

    @Select("SELECT COUNT(*) FROM Chapter WHERE parent_chapter_id = #{chapterId}")
    int countChildChapters(@Param("chapterId") String chapterId);

    @Select("SELECT COUNT(*) FROM Resource WHERE chapter_id = #{chapterId}")
    int countResourcesInChapter(@Param("chapterId") String chapterId);
}