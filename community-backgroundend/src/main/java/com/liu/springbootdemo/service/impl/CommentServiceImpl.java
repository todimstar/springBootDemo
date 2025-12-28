package com.liu.springbootdemo.service.impl;

import com.liu.springbootdemo.POJO.entity.Comment;
import com.liu.springbootdemo.POJO.entity.User;
import com.liu.springbootdemo.common.enums.ErrorCode;
import com.liu.springbootdemo.common.exception.BusinessException;
import com.liu.springbootdemo.mapper.CommentMapper;
import com.liu.springbootdemo.mapper.PostMapper;
import com.liu.springbootdemo.service.CommentService;
import com.liu.springbootdemo.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostMapper postMapper;

    @Override
    public Comment createComment(Long postId,Comment comment) {
        //1.获取当前用户，校验登录与否
        //2.校验帖子是否存在
        //3.校验评论是否存在
        // 校验评论用户和登录用户是否通义
        //3.插入评论，返回

        //空检查
        User currentUser = SecurityUtil.getCurrentUser();
        if(currentUser == null){
            throw new BusinessException(ErrorCode.UNAUTHORIZED,"未登录，请登录后评论");
        }

        if(postMapper.findById(postId) == null){
            throw new BusinessException(ErrorCode.POST_NOT_FOUND,"评论的帖子消失啦！~");
        }

        if(comment == null || !StringUtils.hasText(comment.getContent())){
            throw new BusinessException(ErrorCode.COMMENT_CONTENT_EMPTY);
        }

        //属性检查
//        if(!currentUser.getId().equals(comment.getUserId())){
//            throw new NotAuthorException("与当前登录用户不符，请检查登录");
//        }//不需要检查与当前用户，首先没传来用户，其次这是新建评论，本就是用当前登录的，不用校验也没法校验

        //但是记得赋值
        comment.setPostId(postId);
        comment.setUserId(currentUser.getId());// 之后可能还有ip地址记得在这里加
        if(commentMapper.insert(comment) != 1){
            throw new RuntimeException("评论插入数据库失败！稍后重试");
        }

        return commentMapper.findById(comment.getId());
    }

    @Override
    public List<Comment> findByPostIdByPage(Long postId, int page, int size) {
        // 1.查空
        // 2.查合法
        // 3.调用
        if(postMapper.findById(postId) == null){
            throw new BusinessException(ErrorCode.POST_NOT_FOUND,"查看的帖子不存在了？？！ 你，不应该来这✈️");//应该进不到这步吧，应该帖子页面都进不去调用不了这个评论,不过确实可以用url访问所以还是有必要的拦截
        }

        int index = (page-1)*size;
        return commentMapper.findByPostIdByIndex(postId,index,size);
    }

    @Override
    public void deleteComment(Long commentId) {

        //1.权限足够，已登录和认证用户是评论作者
        //2.评论存在，开始删除
        User currentUser = SecurityUtil.getCurrentUser();
        if(currentUser == null){
            throw new BusinessException(ErrorCode.UNAUTHORIZED,"未登录，请先登录再删除评论~");
        }

        Comment currentComment = commentMapper.findById(commentId);
        if(currentComment == null){
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND,"评论消失了~ 请确认评论id["+commentId+"]是否存在");
        }

        if(!currentComment.getUserId().equals(currentUser.getId())){
            throw new BusinessException(ErrorCode.COMMENT_NOT_AUTHOR,"您不是该评论的作者，不能删除该评论！");
        }

        //你！过关                              ？
        if(commentMapper.delete(commentId)!=1){
            throw new RuntimeException("炸了，评论存不进数据库??!");
        }
    }
}
