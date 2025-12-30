package com.example.demo_app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo_app.entity.Chapter;
import com.example.demo_app.mapper.ChapterMapper;
import com.example.demo_app.mapper.ResourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChapterService extends ServiceImpl<ChapterMapper, Chapter> {

    @Autowired
    private ResourceMapper resourceMapper;

    /**
     * 获取教学班的章节目录树
     */
    public List<Map<String, Object>> getChapterTree(String classId) {
        List<Chapter> allChapters = this.baseMapper.selectChaptersWithParentName(classId);

        // 构建树结构
        Map<String, Map<String, Object>> chapterMap = new HashMap<>();
        List<Map<String, Object>> rootChapters = new ArrayList<>();

        // 将所有章节放入map
        for (Chapter chapter : allChapters) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", chapter.getChapterId());
            node.put("name", chapter.getChapterName());
            node.put("parentId", chapter.getParentChapterId());
            node.put("sortOrder", chapter.getSortOrder());
            node.put("children", new ArrayList<Map<String, Object>>());

            // 统计该章节下的资源数量
            int resourceCount = resourceMapper.countByChapterId(chapter.getChapterId());
            node.put("resourceCount", resourceCount);

            chapterMap.put(chapter.getChapterId(), node);
        }

        // 构建树形结构
        for (Map<String, Object> node : chapterMap.values()) {
            String parentId = (String) node.get("parentId");
            if (parentId == null) {
                rootChapters.add(node);
            } else {
                Map<String, Object> parentNode = chapterMap.get(parentId);
                if (parentNode != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> children = (List<Map<String, Object>>) parentNode.get("children");
                    children.add(node);
                }
            }
        }

        // 对根节点和子节点排序
        rootChapters.sort((a, b) -> {
            Integer orderA = (Integer) a.get("sortOrder");
            Integer orderB = (Integer) b.get("sortOrder");
            if (orderA != null && orderB != null) {
                return orderA.compareTo(orderB);
            }
            return 0;
        });

        // 对每个父节点的子节点排序
        for (Map<String, Object> root : rootChapters) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) root.get("children");
            if (children != null && !children.isEmpty()) {
                children.sort((a, b) -> {
                    Integer orderA = (Integer) a.get("sortOrder");
                    Integer orderB = (Integer) b.get("sortOrder");
                    if (orderA != null && orderB != null) {
                        return orderA.compareTo(orderB);
                    }
                    return 0;
                });
            }
        }

        return rootChapters;
    }

    /**
     * 添加章节
     */
    @Transactional
    public boolean addChapter(Chapter chapter) {
        // 删除这行代码：chapter.setCreateTime(LocalDateTime.now());
        // 因为Chapter实体类已经没有createTime字段了

        // 如果sortOrder为null，设置为1
        if (chapter.getSortOrder() == null) {
            chapter.setSortOrder(1);
        }

        return save(chapter);
    }

    /**
     * 更新章节
     */
    @Transactional
    public boolean updateChapter(Chapter chapter) {
        return updateById(chapter);
    }

    /**
     * 删除章节（级联删除子章节和资源）
     */
    @Transactional
    public boolean deleteChapter(String chapterId) {
        try {
            // 先删除所有子章节
            LambdaQueryWrapper<Chapter> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Chapter::getParentChapterId, chapterId);
            List<Chapter> childChapters = list(wrapper);

            for (Chapter child : childChapters) {
                deleteChapterRecursive(child.getChapterId());
            }

            // 删除本章节
            return removeById(chapterId);
        } catch (Exception e) {
            throw new RuntimeException("删除章节失败: " + e.getMessage());
        }
    }

    /**
     * 递归删除章节及其资源
     */
    private void deleteChapterRecursive(String chapterId) {
        // 删除该章节下的所有资源
        LambdaQueryWrapper<com.example.demo_app.entity.Resource> resourceWrapper = new LambdaQueryWrapper<>();
        resourceWrapper.eq(com.example.demo_app.entity.Resource::getChapterId, chapterId);
        resourceMapper.delete(resourceWrapper);

        // 查找并删除所有子章节
        LambdaQueryWrapper<Chapter> chapterWrapper = new LambdaQueryWrapper<>();
        chapterWrapper.eq(Chapter::getParentChapterId, chapterId);
        List<Chapter> childChapters = list(chapterWrapper);

        for (Chapter child : childChapters) {
            deleteChapterRecursive(child.getChapterId());
        }

        // 删除当前章节
        removeById(chapterId);
    }

    /**
     * 获取教学班的所有章节（平铺列表）
     */
    public List<Chapter> getChaptersByClassId(String classId) {
        LambdaQueryWrapper<Chapter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Chapter::getClassId, classId);
        wrapper.orderByAsc(Chapter::getParentChapterId, Chapter::getSortOrder, Chapter::getChapterName);
        return list(wrapper);
    }

    /**
     * 获取父章节名称
     */
    public String getParentChapterName(String parentChapterId) {
        if (parentChapterId == null) {
            return null;
        }
        Chapter chapter = getById(parentChapterId);
        return chapter != null ? chapter.getChapterName() : null;
    }
}