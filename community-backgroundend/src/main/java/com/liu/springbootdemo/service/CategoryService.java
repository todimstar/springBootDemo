package com.liu.springbootdemo.service;

import com.liu.springbootdemo.POJO.dto.request.CategoryPageQueryDTO;
import com.liu.springbootdemo.POJO.dto.request.CreateCategoryDTO;
import com.liu.springbootdemo.POJO.dto.request.UpdateCategoryDTO;
import com.liu.springbootdemo.POJO.vo.CategoryAdminVO;
import com.liu.springbootdemo.POJO.vo.CategoryVO;
import com.liu.springbootdemo.POJO.vo.Result.PageResult;

import java.util.List;

public interface CategoryService {
    /**
     * 增分区 - 仅管理员
     * @param dto 前端数据
     * @return
     */
    CategoryAdminVO createCategory(CreateCategoryDTO dto);
    /**
     * 用户查找活动分区列表
     */
    List<CategoryVO> ListCategories();

    /**
     * 管理员查找所有分区
     * @return 为CategoryAdminVO
     */
    List<CategoryAdminVO> ListCategoriesForAdmin();

    /**
     * 用id查分区   用于分区展示时
     * @param id
     * @return CategoryVO
     */
    CategoryVO getCategoryById(Long id);



    /**
     * 管理员找分区 -含禁用，用于给管理员展示分区
     * @param id
     * @return CategoryAdminVO
     */
    CategoryAdminVO getCategoryByIdForAdmin(Long id);

    /**
     * 更新分区详情 - 仅管理员
     * @param dto
     * @return
     */
    CategoryAdminVO updateCategory(UpdateCategoryDTO dto);

    /**
     * 更新分区排序权重,批量排序    - 仅管理员
     * @param id
     * @param sortOrder
     */
    void updateSortOrder(Long id,Integer sortOrder);

    /**
     * 启用分区 - 管理员
     * @param id
     */
    void enableCategory(Long id);

    /**
     * 禁用分区 - 管理员
     * @param id
     */
    void disableCategory(Long id);

    /**
     * 删除分区,注意检查分区下没有帖子才能删除 - 仅管理员
     * @param id
     */
    void deleteCategory(Long id);

    /**
     * 分页查分区信息
     * -DTO中isAdmin区分查询禁用分区
     * @param categoryPageQueryDTO
     * @return
     */
    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);
}
