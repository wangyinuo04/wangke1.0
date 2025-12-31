package com.example.demo_app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo_app.entity.PaperQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface PaperQuestionMapper extends BaseMapper<PaperQuestion> {

    @Select("SELECT q.* FROM Question q " +
            "INNER JOIN PaperQuestion pq ON q.question_id = pq.question_id " +
            "WHERE pq.paper_id = #{paperId} " +
            "ORDER BY pq.question_order")
    List<PaperQuestion> getQuestionsByPaper(@Param("paperId") String paperId);
}