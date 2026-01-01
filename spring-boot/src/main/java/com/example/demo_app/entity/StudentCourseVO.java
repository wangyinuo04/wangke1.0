package com.example.demo_app.entity; // 或者 com.example.demo_app.vo

import lombok.Data;

@Data
public class StudentCourseVO {
    private String classId;      // 教学班ID (用于操作，如退课)
    private String courseId;     // 课程ID
    private String courseName;   // 课程名称
    private String teacherName;  // 教师姓名
    private String className;    // 教学班名称 (如：软件2201)
    private String semester;     // 学期
    private Float credit;        // 学分
    private String status;       // 状态 (前端展示用)
}