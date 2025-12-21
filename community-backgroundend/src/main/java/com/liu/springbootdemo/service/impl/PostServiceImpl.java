package com.liu.springbootdemo.service.impl;

import com.liu.springbootdemo.POJO.entity.Post;
import com.liu.springbootdemo.POJO.entity.User;
import com.liu.springbootdemo.exception.InvalidInputException;
import com.liu.springbootdemo.exception.NotFindException;
import com.liu.springbootdemo.exception.NotAuthorException;
import com.liu.springbootdemo.mapper.PostMapper;
import com.liu.springbootdemo.mapper.UserMapper;
import com.liu.springbootdemo.service.PostService;
import com.liu.springbootdemo.utils.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 新建帖子
     * @param post  前端来的帖子
     * @return post 数据库中存的帖子
     */
    @Override
    public Post createPost(Long currentUserId, Post post) {
        // 1. 从SecurityContext获取当前登录用户的信息
//        User currentUser = SecurityUtil.getCurrentUser();

        // 2. 将当前用户的ID设置到post对象中
        post.setUserId(currentUserId);

        // 3. 校验帖子内容
        if(!StringUtils.hasText(post.getTitle())){
            throw new InvalidInputException("帖子标题不能为空");
        }
        if(!StringUtils.hasText(post.getContent())){
            throw new InvalidInputException("帖子内容不能为空");
        }

        // 4. 调用Mapper插入数据库
        postMapper.insert(post);

        return postMapper.findById(post.getId());
    }

    /**
     * 修改帖子
     * @param post
     * @return
     */
    @Override
    public Post updatePost(Long postId, Post post) {
        // NOTE: 要调用帖子Mapper.updateTime更新时间,2.要校验前端不可信id，而校验id我认为用user_id+title，userid用Security验证，数据库里扫一遍该userid下的titile中有没有重名的帖子，没有就报错返回该帖子已不存在，有就更新家调用updateTime
        // 有没有什么快速的方法拿到Security中的userid？还是包装起来 --> 现在不包装
        // --不用手动更新，Update就好

        // 校验需要使用前端来的postId
        // 校验，先使用前端来的postId，查该postId里的userId是不是Security的userid，是则允许修改；
        // 该校验方法优化为单点查询数据库即可，其实也不算信任，只是使用，这不是还在校验嘛，所以不算信任哦

        // 从Security获取当前登录用户
        User currentUser = SecurityUtil.getCurrentUser();

        // 使用前端的postId查帖子后校验帖子是否属于currentUserId
        Post postInDb = postMapper.findById(postId);
        if(postInDb == null){
//            logger.warn("");
            throw new NotFindException("帖子不存在，无法修改");
        }

        //检查修改的帖子是否有要修改的内容
        if (
            (post.getContent()==null || post.getContent().isBlank()) //先插是否为空，再依据肯定是字符串所以查isBlank()，比isEmpty更准确,以防""的出现
            && !StringUtils.hasText(post.getTitle())){              //后续直接用Springboot的StringUtils.hasText也是同理实现
            throw new InvalidInputException("要修改的内容为空");
        }

        // 帖子不归属当前用户    --> TODO:可以加管理员校验实现管理员修改帖子
        if(!postInDb.getUserId().equals(currentUser.getId())){
//            logger.warn("帖子 \"" + post.getTitle() + "\"不属于当前用户[" + currentUser.getUsername() + "]");
            throw new NotAuthorException("帖子 \"" + postInDb.getTitle() + "\"不属于当前用户[" + currentUser.getUsername() + "]");
        }

        // 过关才允许修改
        if( postMapper.updatePost(postId,post) != 1){
            throw new RuntimeException("帖子 \"" + postInDb.getTitle() + "\" 修改失败，数据库修改行数不为1");
        }
        // 直接返回现在的post引用（X）
        // 返回该id从posts里查出来的原文
        return postMapper.findById(postId);
    }

    @Override
    public void deletePost(Long postId) {
        // 1.验证是用户的帖子
        // 2.调用Mapper删除

        // 从Security中获取
        User currentUser = SecurityUtil.getCurrentUser();
        Post postInDB = postMapper.findById(postId);

        if(postInDB == null){
            throw new NotFindException("帖子不存在，无法删除");
        }

        // 比较帖子是否属于当前用户     -->TODO:同样可以加管理员校验，用Security查看用户身份，那就是currentUser的身份
        if(!postInDB.getUserId().equals(currentUser.getId())){
            throw new NotAuthorException("帖子\"" + postInDB.getTitle() + "\"不属于当前用户[" + currentUser.getUsername() + "]");
        }

        //允许删除
        postMapper.deleteById(postId);

    }

    @Override
    public Post getPostById(Long postId) {
        Post postInDb = postMapper.findById(postId);
        if(postInDb == null){
            throw new NotFindException("寻找的帖子不存在捏");
        }
        return postInDb;
    }


    public List<Post> getPostsByPage(int page,int size){
        int index = (page-1)*size;  //头指针，相对数据库首行偏移量
        
        return postMapper.getPostsByPage(index,size);
    }

    @Override
    public List<Post> getAllTitles() {
        return postMapper.getAllTitles();
    }

}
