package com.liu.springbootdemo.mapper;

import com.github.pagehelper.Page;
import com.liu.springbootdemo.POJO.entity.Post;
import com.liu.springbootdemo.POJO.vo.PostSummaryVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 还有三连状态查询，热度查询，新更新，新创建
 */
@Mapper
public interface PostMapper {
    /**
     * 增
     * 添加帖子
     * @param post 新帖子
     * @return 影响的行数，1为成功
     */
    @Insert("INSERT INTO posts(user_id,title,content,summary,create_time,update_time,category_id,category_name,status)"+  //NOTE:1.4加上status    NOTE:12.31 加上分区名  NOTE:12.28 使category_id为必填字段
            "VALUES(#{userId},#{title},#{content},#{summary}, NOW(), NOW(), #{categoryId},#{categoryName},#{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")   //获取数据库主键，并赋给id
    int insert(Post post);

    /**
     * 查
     * 根据id查帖子详情，用于展示帖子，PostDetailVO ≤ Post，可以直接转换
     * 大多数调用都是内部调用或者创作者才能调用，在Service层已经有很多逻辑，数据库不要多条件查询，对于帖子状态的过滤在Service层做
     * @Param id 帖子id
     * @return post 帖子对象
     */
    @Select("SELECT * FROM posts WHERE id = #{id}")
    Post findById(Long id);

    /**
     * 查
     * 根据id查帖子是否存在 - 管理员，所以不排除已删除的帖子
     * @Param id 帖子id
     * @return 存在与否
     */
    @Select("SELECT EXISTS(SELECT 1 FROM posts WHERE id = #{id})")
    boolean isExistById(Long id);

    /**
     * 查
     * 根据id查帖子状态    - 不知道给谁用，但是注意没有排除已删除的帖子
     * @Param id 帖子id
     * @return 状态码 0草稿，1待审核，2已发布，3已拒绝，4已删除
     */
    @Select("SELECT status FROM posts WHERE id = #{id}")
    int findStatusById(Long id);


    /**
     * 分页查,pageHelper已在Service层开启
     * 根据userId查帖子列表，
     * 查的都是SummaryVO，用于用户主页展示帖子列表，所以没填userId字段
     * 动态sql根据传参isAdmin返回不同结果
     * @Param userId 用户id
     * @Param isAdmin 是否管理员
     * @return Page<postSummaryVO> 帖子对象列表
     */ //OK:本接口三状态已实现，正常功能完工
    Page<PostSummaryVO> findPostsByUserId(@Param("userId") Long userId,
                                          @Param("isAdmin") boolean isAdmin,
                                          @Param("isAuthor") boolean isAuthor);

    /**
     * 查
     * 根据categoryId查帖子列表
     * FIXME:限制只返回已发布的帖子，后续可能会有管理员查看分区下所有帖子需求再改，建议统一状态过滤在Service层做
     * 只查询已发布(PUBLISHED=2)的帖子
     * 使用 OGNL 表达式直接引用枚举常量，避免魔法数字，也不需要传参
     * @Param categoryId 分区id
     * @return posts 帖子对象列表
     */
    @Select("SELECT * FROM posts WHERE category_id = #{categoryId} AND status = ${@com.liu.springbootdemo.common.enums.PostStatus@PUBLISHED.getStatus()}")
    List<Post> findPostsByCategoryId(@Param("categoryId")Long categoryId);

    /**
     * 分页查已发布的帖子
     * 返回index下size数量的帖子，用于分页
     * 自动有pageable拦截器处理分页
     * @return List<Post>
     */
    @Select("SELECT p.*, u.username, u.avatar_url as userAvatarUrl " +
            "FROM posts p " +
            "LEFT JOIN users u ON p.user_id = u.id " +
            "WHERE p.status = #{status} " +
            "ORDER BY p.update_time DESC")// DESC降序，越新越前面
    Page<PostSummaryVO> getPostsByPage(@Param("status") int status);

    /**
     * 改
     * 根据帖子id改帖子内容和标题，但需要动态构造title/content更新
     * xml-具体SQL实现见 resources/mapper/PostMapper.xml
     * @param post
     * @return 影响的行数，1为成功
     */
    int updatePost(@Param("id") Long postId, @Param("P") Post post);

    /**
     * 改帖子状态，但是需要不改动帖子的修改时间，因为修改时间是用户的修改为准，状态修改不改变修改时间
     * 显式设置 update_time = update_time 以规避 MySQL 的 ON UPDATE CURRENT_TIMESTAMP 自动更新
     * @param postId
     * @param status
     * @return 影响的行数，1为成功
     */
    @Update("UPDATE posts SET status = #{status},update_time = update_time WHERE id = #{id}")
    int updateStatus(@Param("id") Long postId, @Param("status") int status);

    /**
     * 硬删，管理员
     * 根据id删帖子
     * @Param id 帖子id
     * @return 影响的行数，1为成功
     */
    @Delete("DELETE FROM posts WHERE id = #{id}")
    int deleteById(Long id);


    /**
     * 统计分区下帖子数量(删除和展示时校验一致性使用)
     * @param categoryId 分区id
     * @return size 数量
     */
    @Select("SELECT count(*) FROM posts WHERE category_id = #{categoryId}")
    int countCategoryPostByCategoryId(Long categoryId);

}
