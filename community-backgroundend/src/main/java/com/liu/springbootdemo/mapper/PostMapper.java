package com.liu.springbootdemo.mapper;

import com.liu.springbootdemo.POJO.entity.Post;
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
    @Insert("INSERT INTO posts(user_id,title,content,create_time,update_time)"+
            "VALUES(#{userId},#{title},#{content}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")   //获取数据库主键，并赋给id
    int insert(Post post);

    /**
     * 查
     * 根据id查帖子
     * @Param id 帖子id
     * @return post 帖子对象
     */
    @Select("SELECT * FROM posts WHERE id = #{id}")
    Post findById(Long id);

    /**
     * 查
     * 根据id查帖子状态
     * @Param id 帖子id
     * @return 状态码 0草稿，1待审核，2已发布，3已拒绝，4已删除
     */
    @Select("SELECT status FROM posts WHERE id = #{id}")
    int findStatusById(Long id);


    /**
     * 查
     * 根据userId查帖子列表
     * @Param userId 用户id
     * @return posts 帖子对象列表
     */
    @Select("SELECT * FROM posts WHERE user_id = #{userId}")
    List<Post> findPostsByUserId(@Param("userId")Long userId);

    /**
     * 查
     * 根据categoryId查帖子列表
     * @Param categoryId 分区id
     * @return posts 帖子对象列表
     */
    @Select("SELECT * FROM posts WHERE category_id = #{categoryId}")
    List<Post> findPostsByCategoryId(@Param("categoryId")Long categoryId);

    /**
     * 分页查
     * 返回index下size数量的帖子，用于分页
     * @Param index 偏移量，从该行数据库开始
     * @Param size  读取的行数，往后读取多少行
     * @return List<Post>
     */
    @Select("SELECT * FROM posts ORDER BY update_time DESC LIMIT #{index},#{size}")// DESC降序，越新越前面
    List<Post> getPostsByPage(@Param("index")int index,@Param("size")int size);

    /**
     * 查所有
     * 但最高100？怎么实现查所有但不会导致爆内存呢？因为查所有会很大啊
     * @return
     */
    @Select("SELECT id,title FROM posts ORDER BY update_time DESC")
    List<Post> getAllTitles();

    /**
     * 改
     * 根据帖子id改帖子内容和标题，但需要动态构造title/content更新
     * xml-具体SQL实现见 resources/mapper/PostMapper.xml
     * @param post
     * @return 影响的行数，1为成功
     */
    int updatePost(@Param("postId") Long postId, @Param("P") Post post);

    /**
     * 删
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



    /**
     * 统计某分区下的帖子数（用于删除分区前检查）
     * @param categoryId 分区ID
     * @return 帖子数量
     */
    @Select("SELECT COUNT(*) FROM posts WHERE category_id = #{categoryId}")
    int countByCategoryId(Long categoryId);

}
