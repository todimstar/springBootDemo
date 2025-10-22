package com.liu.springbootdemo.service.impl;

import com.liu.springbootdemo.POJO.dto.request.CreateCategoryDTO;
import com.liu.springbootdemo.POJO.dto.request.UpdateCategoryDTO;
import com.liu.springbootdemo.POJO.vo.CategoryAdminVO;
import com.liu.springbootdemo.POJO.vo.CategoryVO;
import com.liu.springbootdemo.common.enums.ErrorCode;
import com.liu.springbootdemo.entity.Category;
import com.liu.springbootdemo.exception.BusinessException;
import com.liu.springbootdemo.mapper.CategoryMapper;
import com.liu.springbootdemo.mapper.PostMapper;
import com.liu.springbootdemo.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private PostMapper postMapper;


    @Override
    @Transactional  //事务，失败回滚保证数据一致性
    public CategoryAdminVO createCategory(CreateCategoryDTO dto) {
        //鉴权✅，验空(Valid✅)，验重，插入
        //验管理员？在Controller层验证了！
        //验重
        Category category = categoryMapper.findByName(dto.getName());
        if(category != null){
            throw new BusinessException(ErrorCode.CATEGORY_NAME_EXISTS);
        }
        //DTO -> Entity 必须新建，因为到这了的category是null
        category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setIcon(dto.getIcon());
        category.setSortOrder(dto.getSortOrder());
        category.setIsActive(true);

        if(categoryMapper.insert(category)!=1){
            log.error("分区创建失败，数据库插入影响行数不为1");
            throw new BusinessException(ErrorCode.CATEGORY_CREATE_FAILED);
        }
        //Entity -> VO
        return convertToAdminVO(category);
    }

    @Override
    public List<CategoryVO> ListCategories() {
        List<Category> categories = categoryMapper.findAllActive();
        return categories.stream()//转流取出每个元素
                .map(this::convertToVO)//对每个元素执行函数
                .collect(Collectors.toList());//最后收集为List
    }

    @Override
    public List<CategoryAdminVO> ListCategoriesForAdmin() {
        List<Category> categories = categoryMapper.findAll();
        log.debug("AdminVO",categories);
//        System.out.println(categories);
//        System.out.println("以上是Service");
        List<CategoryAdminVO> allCategories = new ArrayList<>();
        for (Category category :categories) {
            allCategories.add(convertToAdminVO(category));
        }
//        System.out.println(allCategories);
//        System.out.println("以上是ServiceTOVO后的");
        return allCategories;
    }

    @Override
    public CategoryVO getCategoryById(Long id) {
        Category category = easyCheckCategoryExistByIdForUser(id,"根据ID找");
        return convertToVO(category);
    }

    @Override
    public CategoryAdminVO getCategoryByIdForAdmin(Long id){
        Category category = easyCheckCategoryExistById(id,"管理员-根据ID找");
        // 用管理员的ToVO
        return convertToAdminVO(category);
    }

    /**
     * 放在Put请求和Patch请求都可以，因为内容更新都有判断后更新
     * @param dto
     * @return
     */
    @Override
    @Transactional
    public CategoryAdminVO updateCategory(UpdateCategoryDTO dto) {
        //鉴权，验空验重，修改
        //验空,保证有id,用@Vaild的DTO已保证了
        Category category = easyCheckCategoryExistById(dto.getId(), "修改分区基本信息时");

        //验重
        if(dto.getName()!=null && !dto.getName().equals(category.getName())){//自身要改名字(传来名字，且不同之前名字)
            Category existingSameNameCategory = categoryMapper.findByName(dto.getName());
            if(existingSameNameCategory != null){
                log.error("要修改的分区名称已存在");
                throw new BusinessException(ErrorCode.CATEGORY_NAME_EXISTS);
            }
        }
        if(dto.getName()!=null) {
            category.setName(dto.getName());
        }
        if(dto.getDescription() != null) {
            category.setDescription(dto.getDescription());
        }
        if(dto.getIcon() != null){
            category.setIcon(dto.getIcon());
        }
        if(categoryMapper.updateBasicInfo(category) != 1){//会不会有正常但确实不等于1的情况？
            log.error("修改分区基本信息失败");
            throw new BusinessException(ErrorCode.CATEGORY_UPDATE_FAILED);
        }

        if(dto.getSortOrder() != null){
            categoryMapper.updateSortOrder(dto.getId(), dto.getSortOrder());
        }

        return convertToAdminVO(categoryMapper.findById(category.getId()));
    }

    @Override
    @Transactional
    public void updateSortOrder(Long id, Integer sortOrder) {
        //验数据库空
        easyCheckCategoryExistByIdNotReturn(id,"修改权重时");
        categoryMapper.updateSortOrder(id,sortOrder);

    }

    @Override
    @Transactional
    public void enableCategory(Long id) {
        easyCheckCategoryExistByIdNotReturn(id,"开启分区时");
        categoryMapper.enable(id);
    }

    @Override
    @Transactional
    public void disableCategory(Long id) {
        easyCheckCategoryExistByIdNotReturn(id,"关闭分区时");
        categoryMapper.disable(id);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        //id非空
        easyCheckCategoryExistById(id,"准备删除时");

        //注意检查数量为空才能删除
        //查分区自身数据
        int countFromCategories = categoryMapper.postCountById(id);
        //查Post有无挂载
        int countFromPosts = postMapper.countCategoryPostByCategoryId(id);

        if(countFromCategories != countFromPosts){
            throw new BusinessException("该分区帖子数量异常，请稍后重试","40200",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if(countFromPosts > 0){
            throw new BusinessException("该分区下还有"+countFromPosts+"个帖子，无法删除，请先迁移或删除帖子","40200",HttpStatus.CONFLICT);
        }
        //删除
        if(categoryMapper.deleteById(id)!=1){
            throw new BusinessException(ErrorCode.CATEGORY_DELETE_FAILED);
        }
    }

    // 私有辅助方法

    /**
     * Entity的Category -> CategoryVO
     * 使用BeanUtils自动复制属性
     * @param category 源数据
     * @return categoryVO
     */
    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category,vo);
        return vo;
    }

    /**
     * Entity的Category -> CategoryAdminVO
     * 使用BeanUtils自动复制属性
     * @param category 源数据
     * @return categoryAdminVO
     */
    private CategoryAdminVO convertToAdminVO(Category category) {
        CategoryAdminVO vo = new CategoryAdminVO();
        BeanUtils.copyProperties(category,vo);
        return vo;
    }


    /**
     * 带时刻的验空，返回Category,专供User权限用findActiveById
     * @param id
     * @param timeStamp
     */
    private Category easyCheckCategoryExistByIdForUser(Long id,String timeStamp){
        //验空
        Category category = categoryMapper.findActiveById(id);
        if(category == null){
            log.error("{},id为{}的分区不存在", timeStamp,id);
            throw new BusinessException(timeStamp+"的分区不存在","404", HttpStatus.NOT_FOUND);
        }return category;
    }

    /**
     * 带时刻的验空，返回Category - 管理员
     * @param id
     * @param timeStamp
     */
    private Category easyCheckCategoryExistById(Long id,String timeStamp){
        //验空
        Category category = categoryMapper.findById(id);
        if(category == null){
            log.error("{},id为{}的分区不存在", timeStamp,id);
            throw new BusinessException(timeStamp+"的分区不存在","404", HttpStatus.NOT_FOUND);
        }return category;
    }

    /**
     * 带时刻的验空，无返回   - 管理员
     * @param id
     * @param timeStamp
     */
    private void easyCheckCategoryExistByIdNotReturn(Long id,String timeStamp){
        //验空
        Category category = categoryMapper.findById(id);
        if(category == null){
            log.error("{},id为{}的分区不存在", timeStamp,id);
            throw new BusinessException(timeStamp+"的分区不存在","404", HttpStatus.NOT_FOUND);
        }
    }
}
