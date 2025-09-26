package com.liu.springbootdemo.mapper;

import com.liu.springbootdemo.entity.Comment;
import org.apache.ibatis.annotations.*;
import org.springframework.security.core.parameters.P;

import java.util.List;

@Mapper
public interface CommentMapper {
    //增
    /**
     * 增，插入新评论
     * @param comment 新评论
     * @return  影响的行数，1为成功
     */
    @Insert("Insert INTO comments(id,post_id,user_id,content,create_time,ip_address)"+
            "VALUES(#{C.id},#{C.postId},#{C.userId},#{C.content}, NOW(), #{C.ipAddress})")
    @Options(useGeneratedKeys = true, keyProperty = "C.id")
    int insert(@Param("C") Comment comment);    //我觉得没问题，这能展示@Param的作用

    //查
    /**
     * 最简单，根据CommentId查评论
     * @Param commentId
     * @return Comment
     */
    @Select("select * FROM comments where id = #{commentId}")
    Comment findById(Long commentId);

    /**
     * 通过postId查找帖子下指定分页与数量的评论,依照时间顺序排列
     * @param postId 帖子id
     * @param index 指定分页偏移量
     * @param size 指定数量
     * @return List<Comment>
     */
    @Select("SELECT * FROM comments where post_id = #{postId} ORDER BY create_time DESC Limit #{index},#{size}")
    List<Comment> findByPostIdByIndex(@Param("postId") Long postId, @Param("index") int index, @Param("size") int size);

    /**
     * //未来在帖子上'仅看我的'功能可以使用，目前先不用，使用前还需要让数据库建立userId,postId的索引加速
     *查
     * 根据userId查找某一帖子下的所有评论
     * 指定分页与数量
     * @Param userId
     * @Param postId
     * @Param index
     * @Param size
     * @return List<Comment>
     */
    @Select("SELECT * FROM comments where user_id = #{userId} AND post_id = #{postId}"+
            "ORDER BY create_time DESC "+
            "Limit #{index},#{size}")
    List<Comment> findByUserIdWithPostIdByPage(Long userId,Long postId,int index,int size);

    /**
     * 查，根据UserId查找其所有评论，但分页，给管理员看的
     * @param userId
     * @param index
     * @param size
     * @return  List<Comment>
     */
    @Select("SELECT * From comments where user_id = #{userId} Limit #{index},#{size}")
    List<Comment> findByUserIdByPage(Long userId,int index,int size);

    //改，评论不能修改，只给删了重发

    //删
    /**
     * 删除
     * 根据评论id删评论
     * @param id
     * @return 影响的行数，1为成功
     */
    @Delete("Delete FROM comments Where id = #{id}")
    int delete(Long id);

}
