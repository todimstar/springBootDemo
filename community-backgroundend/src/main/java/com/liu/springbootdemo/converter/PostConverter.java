package com.liu.springbootdemo.converter;

import com.liu.springbootdemo.POJO.entity.Post;
import com.liu.springbootdemo.POJO.entity.User;
import com.liu.springbootdemo.POJO.vo.PostDetailVO;
import com.liu.springbootdemo.POJO.vo.PostSummaryVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostConverter {

    /**
     * 将 Post 实体转换为 PostDetailVO
     * 属性名相同的会自动映射
     * Post > PostDetailVO 所以不用额外写@Mapping指定
     */
    PostDetailVO toDetailVO(Post post);

    /**
     * 将 Post 实体和 User 实体组合转换为 PostSummaryVO
     * 只需手动处理：1. 名字不同的 2. 两个源里都有的(如 createTime)
     */
    @Mapping(source = "post.id", target = "id") // 两个源都有id，必须区分
    @Mapping(source = "user.username", target = "username") // 其实同名可以省略，但为了清晰保留
    @Mapping(source = "user.avatarUrl", target = "userAvatarUrl") // 名字不同：avatarUrl -> userAvatarUrl
    @Mapping(source = "post.createTime", target = "createTime") // 冲突解决：Post和User都有createTime，指定用Post的
    @Mapping(source = "post.updateTime", target = "updateTime") 
    @Mapping(source = "post.status", target = "status") 
    PostSummaryVO toSummaryVO(Post post, User user);
}
