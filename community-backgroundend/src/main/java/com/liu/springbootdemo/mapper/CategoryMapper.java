package com.liu.springbootdemo.mapper;

import com.github.pagehelper.Page;
import com.liu.springbootdemo.POJO.dto.request.CategoryPageQueryDTO;
import com.liu.springbootdemo.POJO.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {
    /**
     * 新增分区，默认0个帖子数量，默认激活
     * @param category 新分区,name,despcription,icon,sort_order
     * @return 影响的行数，1为成功
     */
    @Insert("INSERT INTO categories(name,description,icon,sort_order,create_time)"+
            "VALUES(#{name},#{description},#{icon},#{sortOrder},#{createTime})")
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insert(Category category);

    // ======列表、ID查询 - 用户======

    /**
     * 查看所有Active分区信息,按排序权重大小
     */
    @Select("SELECT * from categories WHERE is_active = TRUE ORDER BY sort_order DESC")
    List<Category> findAllActive();

    /**
     * 用ID查Active分区信息 - 用户
     * @param id 分区id
     * @return  分区信息
     */
    @Select("SELECT * from categories WHERE id = #{id} and is_active = TRUE")
    Category findActiveById(Long id);

    // ======列表、ID、Name查询 - 管理员======

    /**
     * 查看所有分区信息，管理员功能，包括未激活分区
     */
    @Select("SELECT * FROM categories ORDER BY sort_order DESC")
    List<Category> findAll();

    /**
     * 用ID查分区信息(含禁用) - 管理员
     * @param id 分区id
     * @return  分区信息
     */
    @Select("SELECT * from categories WHERE id = #{id}")
    Category findById(Long id);

    /**
     * 根据分页动态查询分区信息(根据dto判断isAdmin返回is_active和禁用分区)
     * @param dto
     * @return
     */
    Page<Category> pageQuery(CategoryPageQueryDTO dto);

    /**
     * 用Name查分区信息，用在创造和修改分区，仅管理员调用，不用校验isActive
     * @param name 分区名
     * @return  分区信息
     */
    @Select("SELECT * from categories WHERE name = #{name}")
    Category findByName(String name);

    // ===== 查分区下帖子数量 =====

    /**
     * 根据ID查分区中帖子数量,仅活动状态的分区
     */
    @Select("SELECT post_count FROM categories WHERE id = #{id} && is_active=TRUE")
    int postCountByIdWithActive(Long id);

    /**
     * 任意分区下帖子数量，包括禁用的分区，管理员功能
     */
    @Select("SELECT post_count FROM categories WHERE id = #{id}")
    int postCountById(Long id);

    // ===== 管理员删改分区 =====

    /**
     * 更新分区name、description、icon等基本信息
     * @param category 修改的分区信息，注意要传全
     * @return  影响的行数，1为成功
     */
    @Update("UPDATE categories SET name = #{name}, description = #{description}, icon = #{icon} WHERE id = #{id}")
    int updateBasicInfo(Category category);

    //（postCount、sortOrder、isActive是否要单独创建函数？）
    /**
     * 分区帖子数量+1
     */
    @Update("UPDATE categories SET post_count = post_count+1 WHERE id=#{categoryId}")
    int incrementPostCount(Long categoryId);

    /**
     * 分区帖子数量-1
     */
    @Update("UPDATE categories SET post_count = post_count-1 WHERE id=#{categoryId}")
    int decrementPostCount(Long categoryId);

    /**
     * 直接设置分区帖子数量
     * @return 影响的行数
     */
    @Update("UPDATE categories SET post_count = #{postCount} WHERE id = #{categoryId}")
    int setPostCount(Long categoryId,Integer postCount);

    /**
     * 更新分区权重，管理员功能
     * @Param id 分区ID
     * @Param sortOrder 新的排序权重
     * @Return 影响的行数
     */
    @Update("UPDATE categories SET sort_order = #{sortOrder} WHERE id = #{id}")
    int updateSortOrder(Long id,Integer sortOrder);

    /**
     * 启用分区
     */
    @Update("UPDATE categories SET is_active=TRUE WHERE id = #{id}")
    int enable(Long id);

    /**
     * 禁用分区
     */
    @Update("UPDATE categories SET is_active=FALSE WHERE id = #{id}")
    int disable(Long id);

    /**
     * 强制切换启用状态
     * @Param id 分区id
     * @Param isActive 要切换的状态
     * @Retrun 影响的行数
     */
    @Update("UPDATE categories SET is_active = #{isActive} WHERE id = #{id}")
    int switchActive(Long id, Boolean isActive);

    /**
     * 删除分区，注意逻辑检查其下帖子是否清空
     */
    @Delete("DELETE FROM categories WHERE id = #{id}")
    int deleteById(Long id);


}
