package com.liu.springbootdemo.controller;

import com.liu.springbootdemo.POJO.dto.request.CreateCategoryDTO;
import com.liu.springbootdemo.POJO.dto.request.UpdateCategoryDTO;
import com.liu.springbootdemo.POJO.vo.CategoryAdminVO;
import com.liu.springbootdemo.POJO.vo.CategoryVO;
import com.liu.springbootdemo.POJO.vo.Result;
import com.liu.springbootdemo.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/categories")
@Validated
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    //===============查询接口==================

    /**
     * 获取所有启用的分区列表
     * @return 分区列表
     * 权限：公开,访客,用户
     * 场景：首页展示分区
     * 检查：没有
     */
    @GetMapping
    public Result<List<CategoryVO>> ListCategories(){
        List<CategoryVO> categories = categoryService.ListCategories();
        return Result.success(categories);
    }

    /**
     * 获取所有分区，包括禁用
     * 权限：管理员
     * 场景：管理员后台展示和操控
     * 检查：鉴权管理员
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<CategoryAdminVO>> ListCategoriesForAdmin(){
        List<CategoryAdminVO> categories = categoryService.ListCategoriesForAdmin();
        return Result.success(categories);
    }

    /**
     * 根据ID获取启用的分区详情
     * 权限：公开,访客,用户
     * 场景：正常点击分区展示详情
     */
    @GetMapping("/{id}")
    public Result<CategoryVO> getCategoryById(@PathVariable
                                                  @NotNull
                                                  @Min(value = 1,message = "ID必须大于0")
                                                  @Max(value = Long.MAX_VALUE,message = "ID超出有效范围")
                                                  Long id){

        CategoryVO vo = categoryService.getCategoryById(id);
        return Result.success(vo);
    }

    /**
     * 根据ID获取含禁用的分区详情
     * 权限：仅管理员
     * 场景：管理员修改分区展示详情前的展示
     */
    @GetMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<CategoryAdminVO> getCategoryByIdForAdmin(@PathVariable
                                                               @NotNull
                                                               @Min(value = 1,message = "ID必须大于0")
                                                               @Max(value = Long.MAX_VALUE,message = "ID超出有效范围")
                                                               Long id){
        CategoryAdminVO vo = categoryService.getCategoryByIdForAdmin(id);
        return Result.success(vo);
    }

    //===========分区修改-管理员接口===========
    /**
     * 分区的增，管理员检验
     */
    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public Result<CategoryAdminVO> insertCategory(@RequestBody @Valid CreateCategoryDTO categoryDTO){
        CategoryAdminVO categoryVO = categoryService.createCategory(categoryDTO);
        return Result.success("分区创建成功",categoryVO);
    }

    /**
     * 分区信息的改，管理员
     * Put请求可以直接用内存
     */
    @PutMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public Result<CategoryAdminVO> updateCategory(@RequestBody @Valid UpdateCategoryDTO dto){
        CategoryAdminVO categoryVO = categoryService.updateCategory(dto);
        return Result.success("分区修改成功",categoryVO);
    }

    /**
     * 分区的启用，管理员
     */
    @PutMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> enableCategory(@PathVariable @NotNull Long id){
        categoryService.enableCategory(id);
        return Result.success("分区启用成功");
    }

    /**
     * 分区的禁用，管理员
     */
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> disableCategory(@PathVariable @NotNull Long id){
        categoryService.disableCategory(id);
        return Result.success("分区禁用成功");
    }

    /**
     * 分区的权重改，管理员
     * id,sortOrder
     */
    @PutMapping("/{id}/sort")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateSortOrder(
            @PathVariable @NotNull Long id,
            @RequestParam @NotNull
//                    @Max(value = Integer.MAX_VALUE,message = "权重超出int上限，请重新选择权重或删除多余分区后再试")
//                    @Min(value = Integer.MIN_VALUE,message = "权重低于int下限，请重新选择权重")
            Integer sortOrder){
        categoryService.updateSortOrder(id,sortOrder);
        return Result.success("排序权重已更新");
    }

    /**
     * 分区的删，管理员
     * 分区数量校验在Service
     */
    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteCategory(@PathVariable @NotNull Long id){
        categoryService.deleteCategory(id);
        return Result.success("分区删除成功");
    }


}