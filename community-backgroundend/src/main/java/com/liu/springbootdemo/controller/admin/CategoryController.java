package com.liu.springbootdemo.controller.admin;

import com.liu.springbootdemo.POJO.dto.request.CategoryPageQueryDTO;
import com.liu.springbootdemo.POJO.dto.request.CreateCategoryDTO;
import com.liu.springbootdemo.POJO.vo.CategoryAdminVO;
import com.liu.springbootdemo.POJO.vo.Result.PageResult;
import com.liu.springbootdemo.POJO.vo.Result.Result;
import com.liu.springbootdemo.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminCategoryController")
@PreAuthorize("hasRole('ADMIN')")   //不知道放这里修饰整个类的路径可不可以
@RequestMapping("api/admin/categories")
@Validated  //用于非嵌套的调用
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 分区分页查询 - 管理员版-isAdmin=True
     * @param categoryPageQueryDTO
     * @return
     */
    @GetMapping
    @Operation(summary = "分区分页查询-管理员版")
    public Result<PageResult> pageQuery(@Validated CategoryPageQueryDTO categoryPageQueryDTO) {    //代表要走body传递过来
        log.info("管理员查询第{}页的{}项", categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
        categoryPageQueryDTO.setAdmin(true);    //标记一下为管理员，后续数据库查询用于判断
        PageResult pageResult = categoryService.pageQuery(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据ID获取含禁用的分区详情
     * 权限：仅管理员
     * 场景：管理员修改分区展示详情前的展示
     */
    @GetMapping("/{id}")
    public Result<CategoryAdminVO> getCategoryByIdForAdmin(@PathVariable
                                                           @NotNull
                                                           @Min(value = 1,message = "ID必须大于0")
                                                           @Max(value = Long.MAX_VALUE,message = "ID超出有效范围")
                                                           Long id){
        CategoryAdminVO vo = categoryService.getCategoryByIdForAdmin(id); //管理员版
        return Result.success(vo);
    }

    /**
     * 分区的增，管理员检验
     */
    @PostMapping()
    public Result<CategoryAdminVO> insertCategory(@RequestBody @Valid CreateCategoryDTO categoryDTO){
        CategoryAdminVO categoryVO = categoryService.createCategory(categoryDTO);
        return Result.success("分区创建成功",categoryVO);
    }


}
