package com.liu.springbootdemo.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.liu.springbootdemo.POJO.Result.PageResult;
import com.liu.springbootdemo.POJO.dto.CreatePostDTO;
import com.liu.springbootdemo.POJO.entity.Post;
import com.liu.springbootdemo.POJO.entity.User;
import com.liu.springbootdemo.POJO.vo.PostVO;
import com.liu.springbootdemo.common.enums.ErrorCode;
import com.liu.springbootdemo.common.enums.UserRole;
import com.liu.springbootdemo.common.exception.BusinessException;
import com.liu.springbootdemo.mapper.CategoryMapper;
import com.liu.springbootdemo.mapper.PostMapper;
import com.liu.springbootdemo.mapper.UserMapper;
import com.liu.springbootdemo.service.CategoryService;
import com.liu.springbootdemo.service.PostService;
import com.liu.springbootdemo.service.UserService;
import com.liu.springbootdemo.utils.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private UserService userService;
    @Autowired
    private CategoryService categoryService;


    /**
     * 新建帖子 DTO版
     * @param createPostDTO
     * @return
     */
    @Override
    public Post createPost(CreatePostDTO createPostDTO) {
        //1. 从SecurityContext获取当前登录用户的信息
        User currentUser = SecurityUtil.getCurrentUser();

        //post占位待填充进数据库
        Post post = new Post();

        // 2. 将当前用户的ID设置到post对象中    ,不在乎post里的userId，因为不可信
        if (currentUser != null) {
            post.setUserId(currentUser.getId());
            // 填充帖子内容
            BeanUtils.copyProperties(createPostDTO, post);
        }else{
            throw new BusinessException(ErrorCode.UNAUTHORIZED,"Unbelievable! 你是怎么进来的，谁让你没登录就进来的!💢 滚出去😡*");
        }

        // 3. 校验帖子内容时效性，为空已在Controller的@Validated里校验
        //校验分区是否存在，存在才给加帖子，不存在或者锁了的分区不可新增帖子，无论管理员与否
        if(categoryMapper.isActiveById(post.getCategoryId()) != 1){
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND,"分区不存在或已被锁定，无法在该分区下创建帖子");
        }else{
            //分区存在，设置分区名称
            post.setCategoryName(categoryMapper.findNameById(post.getCategoryId()));
        }

        // 4. 调用Mapper插入数据库
        if(postMapper.insert(post) != 1){
            throw new BusinessException(ErrorCode.SQL_ERROR,"帖子\"" + post.getTitle() + "\"创建失败，数据库插入行数不为1");
        }

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
        if(currentUser==null){throw new BusinessException(ErrorCode.UNAUTHORIZED,"Unbelievable! 你是怎么进来的，谁让你没登录就进来的!💢 滚出去😡*");}

        // 使用前端的postId查帖子后校验帖子是否属于currentUser.getId()
        Post postInDb = postMapper.findById(postId);
        if(postInDb == null){
            throw new BusinessException(ErrorCode.POST_NOT_FOUND,"帖子不存在，无法修改");
        }

        // 帖子不归属当前用户    --> TODO:可以加管理员校验实现管理员修改帖子,到时候直接||加上判断currentUser的身份是否是管理员即可
        if (!postInDb.getUserId().equals(currentUser.getId()) && !currentUser.getRole().equals(UserRole.ADMIN.getRoleName())) {
            throw new BusinessException(ErrorCode.POST_NOT_AUTHOR, String.format("帖子 %s 不属于当前用户[%s]",postInDb.getTitle(), currentUser.getUsername()));
        }

        //校验修改内容格式
        //1.验空，检查要修改的post里的Content、Title和CategoryId是否都为空，即没有要修改的内容
        if (
            (post.getContent()==null || post.getContent().isBlank()) //先插是否为空，再依据肯定是字符串所以查isBlank()，比isEmpty更准确,以防""的出现
            && !StringUtils.hasText(post.getTitle())     //标题
            && !StringUtils.hasText(post.getCoverImage())//封面图片
            && post.getCategoryId()==null                //分类ID
        ){
            throw new BusinessException(ErrorCode.INPUT_INVALID,"要修改的内容为空,可以选择删除帖子");
        }
        //2.检查CategoryId是否存在和可用 //OK:修改categroyID后要修改categoryName
        if(post.getCategoryId() != null) { //前端传了才检查并同步修改分区名
            //只用ForUser，因为管理员修改帖子也不能修改到禁用的分区
            categoryService.easyCheckCategoryExistByIdForUser(post.getCategoryId(), "修改帖子时");
            post.setCategoryName(categoryMapper.findNameById(post.getCategoryId()));

        }//TODO:3.上传图片如果需要检查的话这里也要检查

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
            throw new BusinessException(ErrorCode.POST_NOT_FOUND,"帖子不存在，无法删除");
        }

        // 比较帖子是否属于当前用户     -->TODO:同样可以加管理员校验，用Security查看用户身份，那就是currentUser的身份
        if(!postInDB.getUserId().equals(currentUser.getId())){
            throw new BusinessException(ErrorCode.POST_NOT_AUTHOR,"帖子\"" + postInDB.getTitle() + "\"不属于当前用户[" + currentUser.getUsername() + "]");
        }

        //允许删除
        postMapper.deleteById(postId);

    }

    @Override
    public Post getPostById(Long postId) {
        Post postInDb = postMapper.findById(postId);
        if(postInDb == null){
            throw new BusinessException(ErrorCode.POST_NOT_FOUND,"寻找的帖子不存在捏");
        }
        return postInDb;
    }


    @Override
    public List<Post> getPostsByPage(int page, int size){
        int index = (page-1)*size;  //头指针，相对数据库首行偏移量
        
        return postMapper.getPostsByPage(index,size);
    }

    @Override
    public List<Post> getAllTitles() {
        return postMapper.getAllTitles();
    }

    /**
     * 分页获取某用户的帖子,尝试Pageable，会检查用户状态限制是否被封禁
     * -正常用户和管理员都能获取
     * @param userId
     * @param pageable
     * @return PageResult(total, List<Post>)
     */
    @Override
    public PageResult pagePostsByUserId(Long userId, Pageable pageable) {

        //校验用户是否存在和状态是否被封禁
        User userInDb = userService.getUserById(userId); //找不到会抛异常
        if(userInDb.isBanned()){ //状态true为被ban
            throw new BusinessException(ErrorCode.USER_BANNED,"该用户已被封禁，无法获取其帖子列表");
        }

        // 使用PageHelper进行分页
        PageHelper.startPage(pageable.getPageNumber()+1, pageable.getPageSize());//记得+1，因为PageHelper是1-base,Pageable是0-base
        Page<Post> page = postMapper.findPostsByUserId(userId);
        return new PageResult(page.getTotal(),page.getResult());
    }

}
