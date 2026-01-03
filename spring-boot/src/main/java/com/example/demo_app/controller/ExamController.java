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

    // 注入 Mapper 用于直接查询
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

    // 👇👇👇 补上这一行！ 👇👇👇
    @Autowired
    private StudentMapper studentMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ... 后面的代码保持不变 ...
    // ==========================================
    // Part 1: 学生端接口 (考试核心流程)
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
                        vo.put("isSubmitted", true);
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
     * 同时也是教师预览试卷题目的接口（复用）
     */
    @GetMapping("/student/paper/{examId}")
    public Map<String, Object> startExam(@PathVariable String examId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Object userObj = session.getAttribute("user");
        if (userObj == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            Exam exam = examMapper.selectById(examId);
            if (exam == null) throw new Exception("考试不存在");

            // 如果是学生，需要检查时间
            if (userObj instanceof Student) {
                LocalDateTime now = LocalDateTime.now();
                if (now.isBefore(exam.getStartTime())) throw new Exception("考试尚未开始");
                if (now.isAfter(exam.getEndTime())) throw new Exception("考试已结束");

                Student student = (Student) userObj;
                // 创建或获取参与记录
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
            }

            // 拉取试题
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
                    vo.setType(q.getQuestionType());
                    vo.setScore(q.getScore());

                    // 如果是教师，可以返回正确答案以便阅卷时参考（可选）
                    if (userObj instanceof Teacher) {
                        // 可以在 ExamQuestionVO 里加一个 correctAnswer 字段，或者复用其他方式
                        // 这里暂时不加，教师阅卷在前端有单独逻辑显示参考答案
                    }

                    // 解析选项 JSON -> List
                    if (q.getOptions() != null && !q.getOptions().isEmpty()) {
                        try {
                            List<Map<String, Object>> opts = objectMapper.readValue(q.getOptions(), new TypeReference<List<Map<String, Object>>>(){});
                            vo.setOptions(opts);
                        } catch (Exception e) {
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
            Map<String, Object> answers = (Map<String, Object>) params.get("answers");

            Exam exam = examMapper.selectById(examId);
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

                if ("简答".equals(q.getQuestionType())) {
                    subjAnswersMap.put(q.getQuestionId(), studentAns);
                } else {
                    objAnswersMap.put(q.getQuestionId(), studentAns);
                    boolean isCorrect = false;
                    String standardAns = q.getCorrectAnswer();

                    if ("多选".equals(q.getQuestionType())) {
                        if (studentAns instanceof List) {
                            List<String> ansList = (List<String>) studentAns;
                            Collections.sort(ansList);
                            String joined = String.join(",", ansList);
                            if (joined.equalsIgnoreCase(standardAns)) isCorrect = true;
                        }
                    } else {
                        if (studentAns.toString().equalsIgnoreCase(standardAns)) isCorrect = true;
                    }

                    if (isCorrect) {
                        objectiveScore += q.getScore();
                    }
                }
            }

            ExamParticipation part = participationMapper.selectOne(
                    new QueryWrapper<ExamParticipation>()
                            .eq("student_id", student.getStudentId())
                            .eq("exam_id", examId)
            );

            if (part == null) {
                part = new ExamParticipation();
                part.setStudentId(student.getStudentId());
                part.setExamId(examId);
                part.setStartTime(LocalDateTime.now());
                participationMapper.insert(part);
            }

            part.setSubmitTime(LocalDateTime.now());
            part.setObjectiveScore(objectiveScore);

            if (subjAnswersMap.isEmpty()) {
                part.setTotalScore(objectiveScore);
                part.setSubjectiveScore(0f);
                part.setExamStatus("已批改");
            } else {
                part.setExamStatus("已提交");
            }

            part.setObjectiveAnswers(objectMapper.writeValueAsString(objAnswersMap));
            part.setSubjectiveAnswers(objectMapper.writeValueAsString(subjAnswersMap));

            participationMapper.update(part, new QueryWrapper<ExamParticipation>()
                    .eq("student_id", student.getStudentId())
                    .eq("exam_id", examId));

            result.put("success", true);
            result.put("message", "交卷成功");
            result.put("score", objectiveScore);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "提交失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 4. 查看考试结果 (含解析)
     */
    @GetMapping("/student/result/{examId}")
    public Map<String, Object> getExamResult(@PathVariable String examId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Student student = (Student) session.getAttribute("user");

        try {
            Exam exam = examMapper.selectById(examId);
            if (!exam.getShowAnswers()) {
                result.put("success", true);
                result.put("showAnswers", false);
                return result;
            }

            QueryWrapper<PaperQuestion> pqQuery = new QueryWrapper<>();
            pqQuery.eq("paper_id", exam.getPaperId()).orderByAsc("question_order");
            List<PaperQuestion> pqs = paperQuestionMapper.selectList(pqQuery);

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
                item.put("analysis", q.getExplanation());

                Object myAns = myObjAns.get(q.getQuestionId());
                item.put("myAnswer", myAns);

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
    // Part 2: 教师端接口 (补回之前丢失的接口)
    // ==========================================

    /**
     * 获取考试列表（教师端）- 修复 404 错误的关键！
     */
    @GetMapping("/list")
    public Map<String, Object> getExamList(@RequestParam(required = false) String teacherId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 调用 Service 获取列表
            List<Exam> exams = examService.getExamsByTeacher(teacherId);
            result.put("success", true);
            result.put("data", exams);
            result.put("message", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 补全缺失接口：获取某场考试的所有考生成绩列表
     * 对应前端请求: /api/exam/{examId}/submissions
     */
    @GetMapping("/{examId}/submissions")
    public Map<String, Object> getExamSubmissions(@PathVariable String examId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 直接查询参与记录表
            QueryWrapper<ExamParticipation> query = new QueryWrapper<>();
            query.eq("exam_id", examId);
            // 按提交时间倒序
            query.orderByDesc("submit_time");
            List<ExamParticipation> list = participationMapper.selectList(query);

            // 2. 补全学生姓名（因为参与表里只有 student_id）
            for (ExamParticipation p : list) {
                Student s = studentMapper.selectById(p.getStudentId());
                if (s != null) {
                    p.setStudentName(s.getName());
                    // 如果有头像等其他信息也可以在这里设置
                } else {
                    p.setStudentName("未知学生");
                }
            }

            result.put("success", true);
            result.put("data", list);
            result.put("message", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取成绩列表失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取待批改列表
     */
    @GetMapping("/{examId}/pending-grading")
    public Map<String, Object> getPendingGrading(@PathVariable String examId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<ExamParticipation> pendingList = examParticipationService.getPendingGrading(examId);
            // 补充学生姓名
            for (ExamParticipation p : pendingList) {
                Student s = studentMapper.selectById(p.getStudentId());
                p.setStudentName(s != null ? s.getName() : "未知学生");
            }
            result.put("success", true);
            result.put("data", pendingList);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取待批改列表失败");
        }
        return result;
    }

    /**
     * 获取考试统计
     */
    @GetMapping("/{examId}/stats")
    public Map<String, Object> getExamStats(@PathVariable String examId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> stats = examParticipationService.getExamStats(examId);
            result.put("success", true);
            result.put("data", stats);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取统计失败");
        }
        return result;
    }

    /**
     * 提交主观题评分
     */
    @PostMapping("/submission/grade")
    public Map<String, Object> submitGrade(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> result = new HashMap<>();
        try {
            String studentId = (String) requestData.get("studentId");
            String examId = (String) requestData.get("examId");
            // 兼容 Float 和 Integer
            float subjectiveScore = Float.parseFloat(requestData.get("subjectiveScore").toString());

            boolean success = examParticipationService.submitSubjectiveScore(studentId, examId, subjectiveScore);

            if (success) {
                result.put("success", true);
                result.put("message", "评分提交成功");
            } else {
                result.put("success", false);
                result.put("message", "提交失败，记录不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "提交失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 删除考试
     */
    @DeleteMapping("/delete/{examId}")
    public Map<String, Object> deleteExam(@PathVariable String examId) {
        Map<String, Object> result = new HashMap<>();
        try {
            examMapper.deleteById(examId);
            // 注意：实际业务可能需要级联删除参与记录，这里简化处理
            result.put("success", true);
            result.put("message", "删除成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 发布考试 (如果之前有逻辑，请保留，这里提供基础实现)
     */
    /**
     * 发布考试
     * 修复点：自动计算 endTime，处理 ID 生成
     */
    @PostMapping("/publish")
    public Map<String, Object> publishExam(@RequestBody Exam exam) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 生成 ID
            if (exam.getExamId() == null || exam.getExamId().trim().isEmpty()) {
                exam.setExamId(UUID.randomUUID().toString().replace("-", ""));
            }

            // 2. 关键修复：根据“开始时间”和“时长”自动计算“结束时间”
            if (exam.getStartTime() != null && exam.getTimeLimit() != null) {
                // timeLimit 单位是分钟
                exam.setEndTime(exam.getStartTime().plusMinutes(exam.getTimeLimit()));
            } else {
                throw new Exception("开始时间或考试时长不能为空");
            }

            // 3. 插入数据库
            examMapper.insert(exam);

            result.put("success", true);
            result.put("message", "发布成功");
        } catch (Exception e) {
            e.printStackTrace(); // 在控制台打印详细错误，方便调试
            result.put("success", false);
            result.put("message", "发布失败：" + e.getMessage());
        }
        return result;
    }
}