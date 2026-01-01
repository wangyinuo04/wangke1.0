package com.example.demo_app.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo_app.entity.*;
import com.example.demo_app.mapper.*;
import com.example.demo_app.service.ExamParticipationService;
import com.example.demo_app.service.ExamService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/exam")
public class ExamController {

    @Autowired
    private ExamService examService;
    @Autowired
    private ExamParticipationService examParticipationService;

    // === 新增：注入 Mapper 以便直接查询数据 ===
    @Autowired
    private EnrollmentMapper enrollmentMapper;
    @Autowired
    private TeachingClassMapper teachingClassMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private ExamParticipationMapper participationMapper;
    @Autowired
    private PaperQuestionMapper paperQuestionMapper;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private ExamMapper examMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==========================================
    //Part 1: 学生端接口 (新增核心功能)
    // ==========================================

    /**
     * 1. 学生获取我的考试列表
     */
    @GetMapping("/student/list")
    public Map<String, Object> getStudentExamList(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Object userObj = session.getAttribute("user");
        if (userObj == null) {
            result.put("success", false);
            result.put("message", "登录已过期");
            return result;
        }
        Student student = (Student) userObj;

        try {
            // 1. 查询学生加入的班级
            QueryWrapper<Enrollment> enrollQuery = new QueryWrapper<>();
            enrollQuery.eq("student_id", student.getStudentId());
            List<Enrollment> enrollments = enrollmentMapper.selectList(enrollQuery);

            List<String> classIds = enrollments.stream().map(Enrollment::getClassId).collect(Collectors.toList());
            List<Map<String, Object>> voList = new ArrayList<>();

            if (!classIds.isEmpty()) {
                // 2. 查询这些班级的考试
                QueryWrapper<Exam> examQuery = new QueryWrapper<>();
                examQuery.in("class_id", classIds).orderByDesc("start_time");
                List<Exam> exams = examMapper.selectList(examQuery);

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

                for (Exam exam : exams) {
                    Map<String, Object> vo = new HashMap<>();
                    vo.put("id", exam.getExamId());
                    vo.put("title", exam.getExamName());
                    vo.put("startTime", dtf.format(exam.getStartTime()));
                    vo.put("endTime", dtf.format(exam.getEndTime()));
                    vo.put("duration", exam.getTimeLimit());
                    vo.put("showAnswers", exam.getShowAnswers());

                    // 课程名称
                    TeachingClass tc = teachingClassMapper.selectById(exam.getClassId());
                    if (tc != null) {
                        Course c = courseMapper.selectById(tc.getCourseId());
                        vo.put("courseName", c != null ? c.getCourseName() : "未知课程");
                    }

                    // 3. 查询参与状态
                    ExamParticipation part = participationMapper.selectOne(
                            new QueryWrapper<ExamParticipation>()
                                    .eq("student_id", student.getStudentId())
                                    .eq("exam_id", exam.getExamId())
                    );

                    if (part != null) {
                        vo.put("isSubmitted", true); // 有记录即视为已提交/已开始
                        // 如果状态是 "进行中"，其实还没交卷，但这里简化处理，视具体业务而定
                        // 建议：如果 status 是 '进行中'，前端显示 '继续考试'；如果是 '已提交'，显示 '查看成绩'
                        vo.put("status", part.getExamStatus());
                        vo.put("totalScore", part.getTotalScore());
                        vo.put("objScore", part.getObjectiveScore());
                        vo.put("subjScore", part.getSubjectiveScore());
                    } else {
                        vo.put("isSubmitted", false);
                        vo.put("status", "未开始");
                    }
                    voList.add(vo);
                }
            }
            result.put("success", true);
            result.put("data", voList);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 2. 开始考试：获取试卷内容 (隐藏答案)
     */
    @GetMapping("/student/paper/{examId}")
    public Map<String, Object> startExam(@PathVariable String examId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Student student = (Student) session.getAttribute("user");
        if (student == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            Exam exam = examMapper.selectById(examId);
            if (exam == null) throw new Exception("考试不存在");

            // 检查时间
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(exam.getStartTime())) throw new Exception("考试尚未开始");
            if (now.isAfter(exam.getEndTime())) throw new Exception("考试已结束");

            // 1. 创建或获取参与记录
            ExamParticipation part = participationMapper.selectOne(
                    new QueryWrapper<ExamParticipation>()
                            .eq("student_id", student.getStudentId())
                            .eq("exam_id", examId)
            );

            if (part == null) {
                part = new ExamParticipation();
                part.setStudentId(student.getStudentId());
                part.setExamId(examId);
                part.setStartTime(now);
                part.setExamStatus("进行中");
                participationMapper.insert(part);
            } else if ("已提交".equals(part.getExamStatus()) || "已批改".equals(part.getExamStatus())) {
                throw new Exception("您已交卷，无法再次进入");
            }

            // 2. 拉取试题
            QueryWrapper<PaperQuestion> pqQuery = new QueryWrapper<>();
            pqQuery.eq("paper_id", exam.getPaperId()).orderByAsc("question_order");
            List<PaperQuestion> pqs = paperQuestionMapper.selectList(pqQuery);

            List<ExamQuestionVO> questions = new ArrayList<>();
            for (PaperQuestion pq : pqs) {
                Question q = questionMapper.selectById(pq.getQuestionId());
                if (q != null) {
                    ExamQuestionVO vo = new ExamQuestionVO();
                    vo.setId(q.getQuestionId());
                    vo.setStem(q.getContent());
                    vo.setType(q.getQuestionType()); // 单选、多选、判断、简答
                    vo.setScore(q.getScore());

                    // 解析选项 JSON -> List
                    if (q.getOptions() != null && !q.getOptions().isEmpty()) {
                        try {
                            List<Map<String, Object>> opts = objectMapper.readValue(q.getOptions(), new TypeReference<List<Map<String, Object>>>(){});
                            vo.setOptions(opts);
                        } catch (Exception e) {
                            // 兼容旧数据或纯文本
                            vo.setOptions(new ArrayList<>());
                        }
                    }
                    questions.add(vo);
                }
            }

            result.put("success", true);
            result.put("data", questions);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 3. 提交试卷 (自动判分)
     */
    @PostMapping("/student/submit")
    public Map<String, Object> submitExam(@RequestBody Map<String, Object> params, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Student student = (Student) session.getAttribute("user");

        try {
            String examId = (String) params.get("examId");
            // 前端传来的答案: { "questionId1": "A", "questionId2": ["A","B"], ... }
            Map<String, Object> answers = (Map<String, Object>) params.get("answers");

            // 1. 获取考试信息
            Exam exam = examMapper.selectById(examId);

            // 2. 拉取所有题目标准答案
            QueryWrapper<PaperQuestion> pqQuery = new QueryWrapper<>();
            pqQuery.eq("paper_id", exam.getPaperId());
            List<PaperQuestion> pqs = paperQuestionMapper.selectList(pqQuery);

            float objectiveScore = 0f;
            Map<String, Object> objAnswersMap = new HashMap<>();
            Map<String, Object> subjAnswersMap = new HashMap<>();

            for (PaperQuestion pq : pqs) {
                Question q = questionMapper.selectById(pq.getQuestionId());
                Object studentAns = answers.get(q.getQuestionId());

                if (studentAns == null) continue;

                // 区分主客观题
                if ("简答".equals(q.getQuestionType())) {
                    subjAnswersMap.put(q.getQuestionId(), studentAns);
                } else {
                    objAnswersMap.put(q.getQuestionId(), studentAns);
                    // 自动判分逻辑
                    boolean isCorrect = false;
                    String standardAns = q.getCorrectAnswer(); // 假设库里存的是 "A" 或 "A,B" 或 "正确"

                    if ("多选".equals(q.getQuestionType())) {
                        // 多选需要排序对比: 前端传 ["B","A"] -> 转为 "A,B" 对比
                        if (studentAns instanceof List) {
                            List<String> ansList = (List<String>) studentAns;
                            Collections.sort(ansList);
                            String joined = String.join(",", ansList);
                            if (joined.equalsIgnoreCase(standardAns)) isCorrect = true;
                        }
                    } else {
                        // 单选/判断
                        if (studentAns.toString().equalsIgnoreCase(standardAns)) isCorrect = true;
                    }

                    if (isCorrect) {
                        objectiveScore += q.getScore();
                    }
                }
            }

            // 3. 更新数据库
            ExamParticipation part = participationMapper.selectOne(
                    new QueryWrapper<ExamParticipation>()
                            .eq("student_id", student.getStudentId())
                            .eq("exam_id", examId)
            );

            if (part == null) {
                // 防御性代码：如果没有开始记录直接提交
                part = new ExamParticipation();
                part.setStudentId(student.getStudentId());
                part.setExamId(examId);
                part.setStartTime(LocalDateTime.now());
                participationMapper.insert(part);
            }

            part.setSubmitTime(LocalDateTime.now());
            part.setObjectiveScore(objectiveScore);
            // 如果没有简答题，直接总分=客观分，状态=已批改；否则状态=已提交
            if (subjAnswersMap.isEmpty()) {
                part.setTotalScore(objectiveScore);
                part.setSubjectiveScore(0f);
                part.setExamStatus("已批改");
            } else {
                part.setExamStatus("已提交"); // 等待老师批改主观题
            }

            // 保存答案 JSON
            part.setObjectiveAnswers(objectMapper.writeValueAsString(objAnswersMap));
            part.setSubjectiveAnswers(objectMapper.writeValueAsString(subjAnswersMap));

            participationMapper.update(part, new QueryWrapper<ExamParticipation>()
                    .eq("student_id", student.getStudentId())
                    .eq("exam_id", examId));

            result.put("success", true);
            result.put("message", "交卷成功");
            result.put("score", objectiveScore); // 返回客观题得分用于展示

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "提交失败: " + e.getMessage());
        }
        return result;
    }

    // 4. 查看考试结果 (含解析)
    @GetMapping("/student/result/{examId}")
    public Map<String, Object> getExamResult(@PathVariable String examId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Student student = (Student) session.getAttribute("user");

        try {
            Exam exam = examMapper.selectById(examId);
            if (!exam.getShowAnswers()) {
                // 如果老师设置不显示答案
                result.put("success", true);
                result.put("showAnswers", false);
                return result;
            }

            // 获取题目和正确答案
            QueryWrapper<PaperQuestion> pqQuery = new QueryWrapper<>();
            pqQuery.eq("paper_id", exam.getPaperId()).orderByAsc("question_order");
            List<PaperQuestion> pqs = paperQuestionMapper.selectList(pqQuery);

            // 获取我的答案
            ExamParticipation part = participationMapper.selectOne(
                    new QueryWrapper<ExamParticipation>()
                            .eq("student_id", student.getStudentId())
                            .eq("exam_id", examId)
            );

            Map<String, Object> myObjAns = new HashMap<>();
            if (part != null && part.getObjectiveAnswers() != null) {
                myObjAns = objectMapper.readValue(part.getObjectiveAnswers(), Map.class);
            }

            List<Map<String, Object>> analysisList = new ArrayList<>();
            for (PaperQuestion pq : pqs) {
                Question q = questionMapper.selectById(pq.getQuestionId());
                Map<String, Object> item = new HashMap<>();
                item.put("id", q.getQuestionId());
                item.put("stem", q.getContent());
                item.put("type", q.getQuestionType());
                item.put("score", q.getScore());
                item.put("correctAnswer", q.getCorrectAnswer());
                item.put("analysis", q.getExplanation()); // 解析

                Object myAns = myObjAns.get(q.getQuestionId());
                item.put("myAnswer", myAns);

                // 简单判断对错 (仅供展示)
                boolean isCorrect = false;
                if (myAns != null) {
                    String myAnsStr = myAns.toString();
                    if (myAns instanceof List) {
                        List<String> l = (List<String>)myAns;
                        Collections.sort(l);
                        myAnsStr = String.join(",", l);
                    }
                    if (myAnsStr.equalsIgnoreCase(q.getCorrectAnswer())) isCorrect = true;
                }
                item.put("isCorrect", isCorrect);

                analysisList.add(item);
            }

            result.put("success", true);
            result.put("showAnswers", true);
            result.put("data", analysisList);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }


    // ==========================================
    // Part 2: 原有教师端接口 (保留原有代码)
    // ==========================================

    // ... 请保留你原本的 publishExam, getExamList 等方法 ...
    // (由于篇幅原因，这里不重复粘贴，请确保不要删除它们)

    @PostMapping("/publish")
    public Map<String, Object> publishExam(@RequestBody Map<String, Object> requestData, HttpServletRequest request) {
        // ... 原有逻辑 ...
        return null; // 这里只是占位，请保留你原来的完整代码
    }

    // ... 其他原有接口 ...
}