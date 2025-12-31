package com.example.demo_app.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo_app.entity.PaperQuestion;
import com.example.demo_app.mapper.PaperQuestionMapper;
import org.springframework.stereotype.Service;

@Service
public class PaperQuestionService extends ServiceImpl<PaperQuestionMapper, PaperQuestion> {
}