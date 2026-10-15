package com.itheima.knowledge_platform.service.Impl;

import com.itheima.knowledge_platform.constant.MessageConstant;
import com.itheima.knowledge_platform.context.BaseContext;
import com.itheima.knowledge_platform.exception.CategoryErrorException;
import com.itheima.knowledge_platform.exception.CategoryNameExistException;
import com.itheima.knowledge_platform.exception.RequestInformationIncompleteException;
import com.itheima.knowledge_platform.mapper.CategoryMapper;
import com.itheima.knowledge_platform.mapper.DocumentMapper;
import com.itheima.knowledge_platform.mapper.TeamMemberMapper;
import com.itheima.knowledge_platform.pojo.dto.CategoryDTO;
import com.itheima.knowledge_platform.pojo.entity.Category;
import com.itheima.knowledge_platform.pojo.entity.Document;
import com.itheima.knowledge_platform.pojo.entity.TeamMember;
import com.itheima.knowledge_platform.pojo.vo.CategoryTreeVO;
import com.itheima.knowledge_platform.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private TeamMemberMapper teamMemberMapper;
    @Autowired
    private DocumentMapper documentMapper;

    /**
     * 获取分类树
     *
     * @param teamId
     * @return
     */
    @Override
    public List<CategoryTreeVO> tree(Long teamId) {
        // 1. 查询当前团队所有分类
        List<Category> categoryList = categoryMapper.selectByTeamId(teamId);

        //2.转成VO列表
        List<CategoryTreeVO> categoryTreeVOS = categoryList.stream().map(category -> {
            CategoryTreeVO vo = new CategoryTreeVO();
            vo.setId(category.getId());
            vo.setName(category.getName());
            vo.setParentId(category.getParentId());
            vo.setSortOrder(category.getSortOrder());

            return vo;
        }).collect(Collectors.toList());

        // 3. 筛选顶级分类 parentId 为 null
        List<CategoryTreeVO> rootList = categoryTreeVOS.stream()
                .filter(vo -> vo.getParentId() == null)
                .collect(Collectors.toList());

        // 4. 递归组装子节点
        buildTreeChildren(rootList, categoryTreeVOS);

        return rootList;
    }

    /**
     * 递归构建树形
     */
    private void buildTreeChildren(List<CategoryTreeVO> parentList, List<CategoryTreeVO> categoryTreeVOS) {
        for (CategoryTreeVO parent : parentList) {
            // 找当前父节点的所有子节点
            List<CategoryTreeVO> children = categoryTreeVOS.stream()
                    .filter(vo -> parent.getId().equals(vo.getParentId()))
                    .collect(Collectors.toList());

            //赋值子节点
            parent.setChildren(children);
            //递归往下找
            buildTreeChildren(children, categoryTreeVOS);
        }
    }

    /**
     * 创建分类
     *
     * @param categoryDTO
     * @return
     */
    @Override
    public Category create(CategoryDTO categoryDTO) {
        //校验 name 不能为空
        if (categoryDTO.getName() == null) {
            throw new RequestInformationIncompleteException("[create]" + MessageConstant.CATEGORY_NAME_NOT_ALLOWED_NONE);
        }

        //校验同一层级下分类名称不能重复（同一个 parentId 下唯一）
        Category category = categoryMapper.selectExistById(categoryDTO.getParentId(), categoryDTO.getName(),categoryDTO.getTeamId());

        if (category != null) {
            throw new CategoryNameExistException("[create]" + MessageConstant.CATEGORY_NAME_EXIST);
        }

//        如果 parentId 不为空，校验父分类是否存在
        if (categoryDTO.getParentId() != null) {
            category = categoryMapper.selectById(categoryDTO.getParentId());

            if (category == null) {
                throw new CategoryErrorException("[create]" + MessageConstant.PARENT_NOT_FOUND);
            }
        }

        Category category1 = new Category();
        BeanUtils.copyProperties(categoryDTO, category1);
        category1.setCreatedAt(LocalDateTime.now());
        //插入数据到 category 表
        categoryMapper.insert(category1);
        return category1;
    }

    /**
     * 更新分类
     * 核心功能：修改分类的名称、排序序号等基本信息
     *
     * @param id
     * @param categoryDTO
     * @return
     */
    @Override
    public Category update(Long id, CategoryDTO categoryDTO) {
        //1.分类存在性校验：要更新的分类ID必须存在
        Category category = categoryMapper.selectById(id);

        if (category == null) {
            throw new CategoryErrorException(MessageConstant.CATEGORY_NOT_FOUND);
        }

        //2.权限校验：只有分类的创建者或团队管理员才能修改
        Long teamId = category.getTeamId();

        List<TeamMember> teamMembers = teamMemberMapper.selectTeamMemberByTeamId(teamId);

        //查询当前用户是否是团队队员
        List<Long> list = teamMembers.stream()
                .map(teamMember -> teamMember.getUserId())
                .collect(Collectors.toList());

        if (!list.contains(BaseContext.getCurrentUserId())) {
            throw new CategoryErrorException(MessageConstant.USER_NO_PERMISSION_TO_MODIFY);
        }

        //名称唯一性校验：同一父分类下，新名称不能与同级分类重复（跨父分类可以有同名）
        Category category1 = categoryMapper.selectExistById(categoryDTO.getParentId(), categoryDTO.getName(),categoryDTO.getTeamId());

        if (category1 != null) {
            throw new CategoryNameExistException(MessageConstant.CATEGORY_NAME_EXIST);
        }

        //通常只允许更新 name 和 sortOrder，不允许修改 parentId（避免破坏树结构）
        //转换类型
        BeanUtils.copyProperties(categoryDTO, category);
        categoryMapper.updateById(category);

        return category;
    }

    /**
     * 删除分类
     *
     * @param id
     */
    @Override
    public void delete(Long id) {
        //1.分类存在性校验：要更新的分类ID必须存在
        Category category = categoryMapper.selectById(id);

        if (category == null) {
            throw new CategoryErrorException(MessageConstant.CATEGORY_NOT_FOUND);
        }

        //2.权限校验：只有分类的创建者或团队管理员才能修改
        Long teamId = category.getTeamId();

        List<TeamMember> teamMembers = teamMemberMapper.selectTeamMemberByTeamId(teamId);

        //查询当前用户是否是团队队员
        List<Long> list = teamMembers.stream()
                .map(teamMember -> teamMember.getUserId())
                .collect(Collectors.toList());

        if (!list.contains(BaseContext.getCurrentUserId())) {
            throw new CategoryErrorException(MessageConstant.USER_NO_PERMISSION_TO_MODIFY);
        }

        //3.子分类检查：如果该分类下有子分类，禁止删除，提示“请先删除子分类”
        List<Category> categoryList = categoryMapper.selectByParentId(category.getId());

        if (!CollectionUtils.isEmpty(categoryList)) {
            throw new CategoryErrorException(MessageConstant.CATEGORY_EXIST_CHILDREN);
        }

        //关联文档检查：如果该分类下有文档（该分类被文档引用），禁止删除，提示“请先将分类下的文档移动到其他分类或删除文档”
        List<Document> documents = documentMapper.selectByCategoryId(category.getId());

        if (!CollectionUtils.isEmpty(documents)){
            throw new CategoryErrorException(MessageConstant.CATEGORY_EXIST_DOCUMENT);
        }

        categoryMapper.deleteById(category.getId());
    }
}
