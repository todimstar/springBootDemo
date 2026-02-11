package com.liu.springbootdemo.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.liu.springbootdemo.POJO.Result.PageResult;
import com.liu.springbootdemo.POJO.dto.CreatePostDTO;
import com.liu.springbootdemo.POJO.dto.ModerationDecisionResult;
import com.liu.springbootdemo.POJO.entity.Post;
import com.liu.springbootdemo.POJO.entity.User;
import com.liu.springbootdemo.POJO.vo.PostDetailVO;
import com.liu.springbootdemo.POJO.vo.PostSummaryVO;
import com.liu.springbootdemo.common.enums.ErrorCode;
import com.liu.springbootdemo.common.enums.ModerationDecision;
import com.liu.springbootdemo.common.enums.PostStatus;
import com.liu.springbootdemo.common.enums.UserRole;
import com.liu.springbootdemo.common.exception.BusinessException;
import com.liu.springbootdemo.converter.PostConverter;
import com.liu.springbootdemo.mapper.CategoryMapper;
import com.liu.springbootdemo.mapper.PostMapper;
import com.liu.springbootdemo.service.CategoryService;
import com.liu.springbootdemo.service.ModerationDecisionService;
import com.liu.springbootdemo.service.PostService;
import com.liu.springbootdemo.service.UserService;
import com.liu.springbootdemo.common.utils.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Slf4j
public class PostServiceImpl implements PostService {

    @Autowired
    private PostMapper postMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private UserService userService;
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private PostConverter postConverter;

    @Autowired
    private ModerationDecisionService moderationDecisionService;


    /**
     * 新建帖子 DTO版
     * @param createPostDTO
     * @return
     */
    @Override
    public PostDetailVO createPost(CreatePostDTO createPostDTO) {
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
        if(!categoryMapper.isActiveById(post.getCategoryId())){
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND,"分区不存在或已被锁定，无法在该分区下创建帖子");
        }else{
            //分区存在，设置分区名称
            post.setCategoryName(categoryMapper.findNameById(post.getCategoryId()));
        }

        // 4. 调用Mapper插入数据库
        if(postMapper.insert(post) != 1){
            throw new BusinessException(ErrorCode.SQL_ERROR,"帖子\"" + post.getTitle() + "\"创建失败，数据库插入行数不为1");
        }

        // 5. 同步调用审核引擎，根据审核结果设置帖子状态
        String textToModerate = post.getTitle() + " " + post.getContent();
        ModerationDecisionResult moderationResult = moderationDecisionService.decide(
                textToModerate, currentUser, post.getId(), "post");
        int moderatedStatus = moderationResult.getPostStatus();

        // 更新帖子状态为审核结果
        postMapper.updateStatus(post.getId(), moderatedStatus);

        log.info("用户[{}]发布了新帖子: [{}], ID: {}, 审核决策: {}, 状态: {}",
                currentUser.getUsername(), post.getTitle(), post.getId(),
                moderationResult.getDecision().getCode(), moderatedStatus);

        // 被自动拦截的内容返回友好提示
        if (moderationResult.getDecision() == ModerationDecision.AUTO_REJECT) {
            throw new BusinessException(ErrorCode.POST_CONTENT_REJECTED);
        }

        return postConverter.toDetailVO(postMapper.findById(post.getId()));
    }

    /**
     * 修改帖子
     * @param post
     * @return
     */
    @Override
    public PostDetailVO updatePost(Long postId, Post post) {
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

        boolean isAdmin = UserRole.ADMIN.getRoleName().equals(currentUser.getRole());
        boolean isAuthor = postInDb.getUserId().equals(currentUser.getId());
        // 帖子不归属当前用户且不是管理员
        if (!isAuthor && !isAdmin) {
            throw new BusinessException(ErrorCode.POST_NOT_AUTHOR, String.format("帖子 %s 不属于当前用户[%s]",postInDb.getTitle(), currentUser.getUsername()));
        }
        //加验证不能对已删除的帖子修改
        if(postInDb.getStatus() == PostStatus.DELETED.getStatus()){
            if(isAdmin) {throw new BusinessException(ErrorCode.POST_ALREADY_DELETED,"帖子已删除，无法修改");}
            //非管理员直接报不存在
            throw new BusinessException(ErrorCode.POST_NOT_FOUND,"帖子不存在，无法修改");
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

        // 内容变更时重新触发审核（标题或正文有修改）
        boolean contentChanged = StringUtils.hasText(post.getTitle()) || StringUtils.hasText(post.getContent());
        if (contentChanged) {
            Post updatedPost = postMapper.findById(postId);
            String textToModerate = updatedPost.getTitle() + " " + updatedPost.getContent();
            ModerationDecisionResult moderationResult = moderationDecisionService.decide(
                    textToModerate, currentUser, postId, "post");
            int moderatedStatus = moderationResult.getPostStatus();

            postMapper.updateStatus(postId, moderatedStatus);

            log.info("用户[{}]修改了帖子: [{}], ID: {}, 重新审核决策: {}, 状态: {}",
                    currentUser.getUsername(), postInDb.getTitle(), postId,
                    moderationResult.getDecision().getCode(), moderatedStatus);

            if (moderationResult.getDecision() == ModerationDecision.AUTO_REJECT) {
                throw new BusinessException(ErrorCode.POST_CONTENT_REJECTED);
            }
        } else {
            log.info("用户[{}]修改了帖子: [{}], ID: {}（非内容变更，跳过审核）",
                    currentUser.getUsername(), postInDb.getTitle(), postId);
        }

        return postConverter.toDetailVO(postMapper.findById(postId));
    }

    /**
     * 删除帖子
     * v1.1 新增检查用户身份，管理员直接物理删除，作者软删除 2026.1.4
     * @param postId
     */
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
        if(currentUser == null){
            throw new BusinessException(ErrorCode.UNAUTHORIZED,"Unbelievable! 你是怎么进来的，谁让你没登录就进来的!💢 滚出去😡*");
        }
        boolean isAdmin = currentUser.getRole().equals(UserRole.ADMIN.getRoleName());
        boolean isAuthor = currentUser.getId().equals(postInDB.getUserId());

        // 比较帖子是否属于当前用户
        if(!isAuthor && !isAdmin){
            throw new BusinessException(ErrorCode.POST_NOT_AUTHOR,"帖子\"" + postInDB.getTitle() + "\"不属于当前用户[" + currentUser.getUsername() + "]");
        }
        if(isAdmin){
            //管理员直接物理删除
            if(postMapper.deleteById(postId) != 1){
                throw new BusinessException(ErrorCode.SQL_ERROR,"帖子\"" + postInDB.getTitle() + "\"删除失败，数据库删除行数不为1");
            }
            log.warn("管理员[{}]物理删除了帖子: [{}], ID: {}", currentUser.getUsername(), postInDB.getTitle(), postId);
        }else{
            //作者软删除
            if(postMapper.updateStatus(postInDB.getId(),PostStatus.DELETED.getStatus()) != 1){
                throw new BusinessException(ErrorCode.SQL_ERROR,"帖子\"" + postInDB.getTitle() + "\"删除失败，数据库更新行数不为1");
            }
            log.info("用户[{}]软删除了帖子: [{}], ID: {}", currentUser.getUsername(), postInDB.getTitle(), postId);
        }

    }

    /**
     * 管理员设置帖子状态
     * @param postId 帖子ID
     * @param status 状态码 0草稿，1待审核，2已发布，3已拒绝，4已删除
     */
    @Override
    public void setPostStatus(Long postId, int status) {
        if(postMapper.isExistById(postId)){
            postMapper.updateStatus(postId,status);
            // 虽然这里拿不到当前操作用户，但这种管理操作通常建议记录
            log.info("帖子ID:[{}] 状态变更为: {}", postId, status);
        }else{
            throw new BusinessException(ErrorCode.POST_NOT_FOUND,"帖子不存在，无法修改状态");
        }
    }

    /**
     * 根据id获取帖子详情，用于展示帖子，PostDetailVO ≤ Post，可以直接转换
     * 但是要区分作者和非作者和管理员
     * @param postId
     * @return PostDetailVO
     */
    @Override
    public PostDetailVO getPostById(Long postId) {
        Post postInDb = postMapper.findById(postId);
        if(postInDb == null){
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        // 权限控制：非公开状态（如1审核中，3已拒绝），只有作者和管理员能看；软删除状态（4已删除）只有管理员能看
        if(postInDb.getStatus() != PostStatus.PUBLISHED.getStatus()){
            User currentUser = SecurityUtil.getCurrentUser();
            //对于未登录用户，直接不允许看到非公开状态帖子
            if(currentUser == null){
                throw new BusinessException(ErrorCode.POST_NOT_FOUND);
            }
            boolean isAdmin = currentUser.getRole().equals(UserRole.ADMIN.getRoleName());
            boolean isAuthor = currentUser.getId().equals(postInDb.getUserId());
            //0草稿：拦截非作者和管理员,只有作者能看
            if(postInDb.getStatus() == PostStatus.DRAFT.getStatus() && !isAuthor ){
                throw new BusinessException(ErrorCode.POST_NOT_FOUND);
            }
            //4软删除：拦截非管理员，只有管理员能看
            if(postInDb.getStatus() == PostStatus.DELETED.getStatus() && !isAdmin){
                throw new BusinessException(ErrorCode.POST_NOT_FOUND);
            }
            //1审核中和3已拒绝：拦截非作者和非管理员
            if(!isAdmin && !isAuthor){
                //亮点：隐蔽式拒绝。对于无权访问的资源，报404而不是403，防止恶意用户通过ID遍历探测哪些ID是存在的
                throw new BusinessException(ErrorCode.POST_NOT_FOUND);
            }
        }
//        else {//有可能会增加前端显示困难，还是算了
//            //公开状态，去除status字段返回
//            postInDb.setStatus(114514); //随便一个前端不认识的数字，表示公开状态
//        }
        return postConverter.toDetailVO(postInDb);
    }


    /**
     * 分页获取帖子已发布的概要列表，Mapper已经LeftJoin和过滤状态
     * @param pageable
     * @return
     */
    @Override
    public PageResult pagePostSummary(Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber()+1,pageable.getPageSize());
        // 传入已发布状态码 (2)
        Page<PostSummaryVO> voPage = postMapper.getPostsByPage(PostStatus.PUBLISHED.getStatus());
        return new PageResult(voPage.getTotal(),voPage.getResult());
    }

    /**
     * 分页获取某用户的帖子概要列表
     * 会检查用户状态限制是否被封禁
     * 查的都是SummaryVO，用于用户主页展示帖子列表，所以没填userId字段
     * -正常用户都能获取
     * //多给mapper传参动态sql区分非用户本身和用户自己和管理员身份
     * 一共三种状态，游客和普通用户只能看已发布的，作者能看除已删除外的所有自己的帖子，管理员能看所有帖
     * 身份是用户自身则排除已删除(DELETED=4)的帖子
     * 管理员则不排除任何状态的帖子   2026.1.5
     * TODO:后期细化三种角色，现在先用简陋的if-else实现，到时候搞个状态机好了，🤮😅
     * @param userId
     * @param pageable
     * @return PageResult(total, List<PostSummaryVO>)，SummaryVO但是不返回用户名和url
     */ //OK:本接口三状态已实现，正常功能完工
    @Override
    public PageResult pagePostsByUserId(Long userId, Pageable pageable) {

        //校验要查找的用户是否存在和状态是否被封禁
        User userInDb = userService.getUserById(userId); //找不到会抛异常
        if(userInDb.isBanned()){ //状态true为被ban
            throw new BusinessException(ErrorCode.USER_BANNED,"该用户已被封禁，无法获取其帖子列表");
        }
        //检查发起询问的管理员与否,同时允许未登录用户依照非管理员身份查询
        boolean isAdmin = false,isAuthor = false;
        User currentUser = SecurityUtil.getCurrentUser();
        if(currentUser != null){
            isAdmin = UserRole.ADMIN.getRoleName().equals(currentUser.getRole());//更安全的equals
            isAuthor = userId.equals(currentUser.getId());
        }

        // 使用PageHelper进行分页
        PageHelper.startPage(pageable.getPageNumber()+1, pageable.getPageSize());//记得+1，因为PageHelper是1-base,Pageable是0-base
        Page<PostSummaryVO> page = postMapper.findPostsByUserId(userId,isAdmin,isAuthor);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 游标分页获取帖子列表
     * @param cursor 上一页最后一条ID
     * @param size 每页条数
     * @return 列表
     */
    @Override
    public List<PostSummaryVO> getPostsByCursor(Long cursor, int size) {
        // 限制每页最大数量，防止恶意请求
        if(size > 100) size = 100;
        return postMapper.getPostsByCursor(cursor, size);
    }
}
