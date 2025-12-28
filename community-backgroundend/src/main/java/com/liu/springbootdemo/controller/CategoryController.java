package com.liu.springbootdemo.controller;

import com.liu.springbootdemo.POJO.dto.request.UpdateCategoryDTO;
import com.liu.springbootdemo.POJO.vo.CategoryAdminVO;
import com.liu.springbootdemo.POJO.vo.CategoryVO;
import com.liu.springbootdemo.POJO.Result.Result;
import com.liu.springbootdemo.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/categories")
@Validated
@Tag(name = "分区管理接口",description = "用户的分区增删改查，残留有部分管理员权限接口")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    //===============查询接口==================

    /**
     * 获取所有启用的分区列表 - TODO:分页查询分区列表
     * @return 分区列表
     * 权限：公开,访客,用户
     * 场景：首页展示分区
     * 检查：没有
     */
    @GetMapping
    @Operation(summary = "分页查询所有启用的分区列表",description = "无需权限，公开接口")
    @SecurityRequirements() // 标记此接口不需要鉴权
    public Result<List<CategoryVO>> ListCategories(){   //NOTE:如果想上admin的分页查询，用到CategoryPageQueryDTO，记得手动设置isAdmin=false保证只查启用
        List<CategoryVO> categories = categoryService.ListCategories();
        return Result.success(categories);
    }

    /**
     * 获取所有分区，包括禁用
     * 权限：管理员
     * 场景：管理员后台展示和操控
     * 检查：鉴权管理员
     * @deprecated 接口废弃⚠️,转至Admin.CategoryController
     */
    @Deprecated(since = "v1.1", forRemoval = true)
    @GetMapping("/admin")               //TODO:废弃⚠️
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有分区列表（含禁用）",description = "仅管理员权限接口,接口废弃⚠️,转至Admin.CategoryController")
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
    @Operation(summary = "根据ID获取启用的分区详情",description = "无需权限，公开接口")
    @SecurityRequirements() // 标记此接口不需要鉴权
    public Result<CategoryVO> getCategoryById(@PathVariable
                                                  @NotNull
                                                  @Min(value = 1L,message = "ID必须大于0")
                                                  @Max(value = Long.MAX_VALUE,message = "ID超出有效范围")
                                                  Long id){

        CategoryVO vo = categoryService.getCategoryById(id);
        return Result.success(vo);
    }

    /**
     * 根据ID获取含禁用的分区详情
     * 权限：仅管理员
     * 场景：管理员修改分区展示详情前的展示
     * @deprecated 接口废弃⚠️,转至Admin.CategoryController
     */
    @Deprecated
    @GetMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")   //TODO:接口废弃⚠️,转至Admin.CategoryController
    @Operation(summary = "根据ID获取含禁用的分区详情-管理员版",description = "仅管理员权限接口,接口废弃⚠️,转至Admin.CategoryController")
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
     * 分区信息的改，管理员
     * Put请求可以直接用内存
     */
    @PutMapping()
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "分区信息的修改-管理员版",description = "仅管理员权限接口")
    public Result<CategoryAdminVO> updateCategory(@RequestBody @Valid UpdateCategoryDTO dto){
        CategoryAdminVO categoryVO = categoryService.updateCategory(dto);
        return Result.success("分区修改成功",categoryVO);
    }

    /**
     * 分区的启用，管理员
     */
    @PutMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "分区的启用-管理员版",description = "仅管理员权限接口")
    public Result<Void> enableCategory(@PathVariable @NotNull Long id){
        categoryService.enableCategory(id);
        return Result.success("分区启用成功");
    }

    /**
     * 分区的禁用，管理员
     */
    @PutMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "分区的禁用-管理员版", description = "仅管理员权限接口")
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
    @Operation(summary = "分区的权重修改-管理员版",description = "仅管理员权限接口")
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
    @Operation(summary = "分区的删除-管理员版",description = "仅管理员权限接口")
    public Result<Void> deleteCategory(@PathVariable @NotNull Long id){
        categoryService.deleteCategory(id);
        return Result.success("分区删除成功");
    }


}