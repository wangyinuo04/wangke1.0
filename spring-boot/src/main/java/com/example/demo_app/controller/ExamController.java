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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    @Autowired
    private StudentMapper studentMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

                    // --- 核心修复：智能解析选项 ---
                    String optionsStr = q.getOptions();
                    List<Map<String, Object>> opts = new ArrayList<>();

                    if (optionsStr != null && !optionsStr.trim().isEmpty()) {
                        try {
                            // 1. 尝试按 JSON 解析 (兼容旧数据)
                            opts = objectMapper.readValue(optionsStr, new TypeReference<List<Map<String, Object>>>(){});
                        } catch (Exception e) {
                            // 2. 如果 JSON 失败，说明是文本格式 (如 "A. xxx\nB. xxx")，手动解析
                            opts = parseOptionsFromText(optionsStr);
                        }
                    }
                    vo.setOptions(opts);
                    // ---------------------------

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
     * 【辅助方法】解析文本格式的选项
     */
    private List<Map<String, Object>> parseOptionsFromText(String text) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (text == null) return list;

        // 按换行符分割
        String[] lines = text.split("\n");
        Pattern pattern = Pattern.compile("^([A-Z])[:\\.\\、\\s]\\s*(.*)");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                Map<String, Object> map = new HashMap<>();
                map.put("key", matcher.group(1)); // 选项字母
                map.put("val", matcher.group(2)); // 选项内容
                list.add(map);
            } else {
                // 如果匹配不到 A. xxx 格式，尝试作为纯文本选项处理（兼容性）
                // 某些情况下可能没有选项头
                Map<String, Object> map = new HashMap<>();
                map.put("key", "");
                map.put("val", line);
                list.add(map);
            }
        }
        return list;
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
                            // 简单比较，实际可能需要去除空格
                            if (joined.replace(" ", "").equalsIgnoreCase(standardAns.replace(" ", ""))) isCorrect = true;
                        }
                    } else {
                        if (studentAns.toString().trim().equalsIgnoreCase(standardAns.trim())) isCorrect = true;
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
                    if (myAnsStr.trim().equalsIgnoreCase(q.getCorrectAnswer().trim())) isCorrect = true;
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
    // Part 2: 教师端接口
    // ==========================================

    @GetMapping("/list")
    public Map<String, Object> getExamList(@RequestParam(required = false) String teacherId) {
        Map<String, Object> result = new HashMap<>();
        try {
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

    @GetMapping("/{examId}/submissions")
    public Map<String, Object> getExamSubmissions(@PathVariable String examId) {
        Map<String, Object> result = new HashMap<>();
        try {
            QueryWrapper<ExamParticipation> query = new QueryWrapper<>();
            query.eq("exam_id", examId);
            query.orderByDesc("submit_time");
            List<ExamParticipation> list = participationMapper.selectList(query);

            for (ExamParticipation p : list) {
                Student s = studentMapper.selectById(p.getStudentId());
                if (s != null) {
                    p.setStudentName(s.getName());
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

    @GetMapping("/{examId}/pending-grading")
    public Map<String, Object> getPendingGrading(@PathVariable String examId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<ExamParticipation> pendingList = examParticipationService.getPendingGrading(examId);
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

    @PostMapping("/submission/grade")
    public Map<String, Object> submitGrade(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> result = new HashMap<>();
        try {
            String studentId = (String) requestData.get("studentId");
            String examId = (String) requestData.get("examId");
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

    @DeleteMapping("/delete/{examId}")
    public Map<String, Object> deleteExam(@PathVariable String examId) {
        Map<String, Object> result = new HashMap<>();
        try {
            examMapper.deleteById(examId);
            result.put("success", true);
            result.put("message", "删除成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/publish")
    public Map<String, Object> publishExam(@RequestBody Exam exam) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (exam.getExamId() == null || exam.getExamId().trim().isEmpty()) {
                exam.setExamId(UUID.randomUUID().toString().replace("-", ""));
            }
            if (exam.getStartTime() != null && exam.getTimeLimit() != null) {
                exam.setEndTime(exam.getStartTime().plusMinutes(exam.getTimeLimit()));
            } else {
                throw new Exception("开始时间或考试时长不能为空");
            }
            examMapper.insert(exam);
            result.put("success", true);
            result.put("message", "发布成功");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "发布失败：" + e.getMessage());
        }
        return result;
    }
}