package com.liu.springbootdemo.common.enums;

import lombok.Getter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 统一错误码枚举
 *
 * 错误码规范：
 * - 00000: 成功
 * - A0001-A9999: 系统级错误
 * - 10000-19999: 用户相关错误
 * - 20000-29999: 认证授权错误
 * - 30000-39999: 参数校验错误
 * - 40000-49999: 业务逻辑错误
 * - 50000-59999: 第三方服务错误
 *
 * @author Liu
 * @date 2025
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ==================== 成功响应 ====================
    SUCCESS("00000", "操作成功", HttpStatus.OK),

    // ==================== 系统级错误 A0001-A9999 ====================
    SYSTEM_ERROR("A0001", "系统执行出错", HttpStatus.INTERNAL_SERVER_ERROR),
    SYSTEM_BUSY("A0002", "系统繁忙，请稍后重试", HttpStatus.SERVICE_UNAVAILABLE),
    SYSTEM_TIMEOUT("A0003", "系统执行超时", HttpStatus.REQUEST_TIMEOUT),
    SYSTEM_RESOURCE_ERROR("A0004", "系统资源异常", HttpStatus.INTERNAL_SERVER_ERROR),
    SQL_ERROR("A0005", "数据库操作异常", HttpStatus.INTERNAL_SERVER_ERROR),

    // ==================== 用户相关错误 10000-19999 ====================
    // 用户基本错误 10001-10099
    USER_NOT_FOUND("10001", "用户不存在", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS("10002", "用户已存在", HttpStatus.CONFLICT),
    USER_STATUS_ABNORMAL("10003", "用户状态异常", HttpStatus.FORBIDDEN),
    USER_BANNED("10004", "用户已被封禁", HttpStatus.FORBIDDEN),
    USER_UPDATE_FAILED("10005", "用户信息更新失败", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_DELETE_FAILED("10006", "用户删除失败", HttpStatus.INTERNAL_SERVER_ERROR),

    // 用户注册错误 10100-10199
    USERNAME_EXISTS("10101", "用户名已被占用", HttpStatus.CONFLICT),
    EMAIL_EXISTS("10102", "邮箱已被注册", HttpStatus.CONFLICT),
    PHONE_EXISTS("10103", "手机号已被注册", HttpStatus.CONFLICT),
    PASSWORD_TOO_SHORT("10104", "密码长度不能少于6位", HttpStatus.BAD_REQUEST),
    PASSWORD_TOO_SIMPLE("10105", "密码过于简单", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID("10106", "用户名格式不正确", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID("10107", "邮箱格式不正确", HttpStatus.BAD_REQUEST),

    //用户端输入错误 10200-10299
    EMPTY_USERNAME("10201", "用户名不能为空", HttpStatus.BAD_REQUEST),
    EMPTY_EMAIL("10202","邮箱不能为空", HttpStatus.BAD_REQUEST),
    EMPTY_PASSWORD("10203", "密码不能为空", HttpStatus.BAD_REQUEST),
    EMPTY_USERNAME_OR_EMAIL("10204", "用户名或邮箱不能为空", HttpStatus.BAD_REQUEST),
    INPUT_INVALID("10205", "输入不合法", HttpStatus.BAD_REQUEST),


    // ==================== 认证授权错误 20000-29999 ====================
    // 认证错误 20001-20099
    UNAUTHORIZED("20001", "未登录或登录已过期", HttpStatus.UNAUTHORIZED),
    WRONG_PASSWORD("20002", "用户名或密码错误", HttpStatus.UNAUTHORIZED),
    ACCOUNT_NOT_FOUND("20003", "账号不存在", HttpStatus.UNAUTHORIZED),
    ACCOUNT_DISABLED("20004", "账号已被禁用", HttpStatus.UNAUTHORIZED),
    ACCOUNT_EXPIRED("20005", "账号已过期", HttpStatus.UNAUTHORIZED),
    FAILED_LOGIN_ATTEMPTS_EXCEEDED("20006", "登录失败次数过多，账号已被锁定", HttpStatus.LOCKED),

    // Token相关 20100-20199
    TOKEN_EXPIRED("20101", "登录已过期，请重新登录", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("20102", "无效的登录凭证", HttpStatus.UNAUTHORIZED),
    TOKEN_MISSING("20103", "缺少登录凭证", HttpStatus.UNAUTHORIZED),
    TOKEN_REFRESH_FAILED("20104", "刷新登录状态失败", HttpStatus.UNAUTHORIZED),

    // 权限错误 20200-20299
    FORBIDDEN("20201", "无权限访问", HttpStatus.FORBIDDEN),
    ROLE_NOT_FOUND("20202", "角色不存在", HttpStatus.NOT_FOUND),
    PERMISSION_DENIED("20203", "权限不足", HttpStatus.FORBIDDEN),

    // ==================== 参数校验错误 30000-39999 ====================
    PARAM_ERROR("30001", "参数错误", HttpStatus.BAD_REQUEST),
    PARAM_MISSING("30002", "缺少必要参数", HttpStatus.BAD_REQUEST),
    PARAM_TYPE_ERROR("30003", "参数类型错误", HttpStatus.BAD_REQUEST),
    PARAM_FORMAT_ERROR("30004", "参数格式错误", HttpStatus.BAD_REQUEST),
    PARAM_OUT_OF_RANGE("30005", "参数值超出允许范围", HttpStatus.BAD_REQUEST),
    PARAM_DUPLICATE("30006", "参数重复", HttpStatus.BAD_REQUEST),

    // ==================== 业务错误 40000-49999 ====================
    // 帖子相关 40001-40099
    POST_NOT_FOUND("40001", "帖子不存在", HttpStatus.NOT_FOUND),
    POST_NOT_AUTHOR("40002", "您不是该帖子的作者", HttpStatus.FORBIDDEN),
    POST_ALREADY_DELETED("40003", "帖子已被删除", HttpStatus.GONE),
    POST_LOCKED("40004", "帖子已被锁定", HttpStatus.FORBIDDEN),
    POST_TITLE_EMPTY("40011", "帖子标题不能为空", HttpStatus.BAD_REQUEST),
    POST_CONTENT_EMPTY("40012", "帖子内容不能为空", HttpStatus.BAD_REQUEST),
    POST_TITLE_TOO_LONG("40013", "帖子标题过长", HttpStatus.BAD_REQUEST),
    POST_CONTENT_TOO_LONG("40014", "帖子内容过长", HttpStatus.BAD_REQUEST),

    // 评论相关 40100-40199
    COMMENT_NOT_FOUND("40101", "评论不存在", HttpStatus.NOT_FOUND),
    COMMENT_NOT_AUTHOR("40102", "您不是该评论的作者", HttpStatus.FORBIDDEN),
    COMMENT_ALREADY_DELETED("40103", "评论已被删除", HttpStatus.GONE),
    COMMENT_CONTENT_EMPTY("40111", "评论内容不能为空", HttpStatus.BAD_REQUEST),
    COMMENT_CONTENT_TOO_LONG("40112", "评论内容过长", HttpStatus.BAD_REQUEST),
    COMMENT_TOO_FREQUENT("40113", "评论过于频繁，请稍后再试", HttpStatus.TOO_MANY_REQUESTS),

    // 分区相关 40200-40299
    CATEGORY_NOT_FOUND("40201", "分区不存在", HttpStatus.NOT_FOUND),
    CATEGORY_NAME_EXISTS("40202", "分区名称已存在", HttpStatus.CONFLICT),
    CATEGORY_HAS_POSTS("40203", "分区下还有帖子，无法删除", HttpStatus.CONFLICT),
    CATEGORY_COUNT_MISMATCH("40204", "分区帖子数量异常,请稍后重试", HttpStatus.INTERNAL_SERVER_ERROR),
    CATEGORY_CREATE_FAILED("40205", "创建分区失败", HttpStatus.INTERNAL_SERVER_ERROR),
    CATEGORY_UPDATE_FAILED("40206", "更新分区失败", HttpStatus.INTERNAL_SERVER_ERROR),
    CATEGORY_DELETE_FAILED("40207", "删除分区失败", HttpStatus.INTERNAL_SERVER_ERROR),

    // 点赞收藏 40300-40399
    ALREADY_LIKED("40301", "您已经点过赞了", HttpStatus.CONFLICT),
    NOT_LIKED("40302", "您还没有点赞", HttpStatus.BAD_REQUEST),
    ALREADY_COLLECTED("40303", "您已经收藏过了", HttpStatus.CONFLICT),
    NOT_COLLECTED("40304", "您还没有收藏", HttpStatus.BAD_REQUEST),

    // 关注相关 40400-40499
    ALREADY_FOLLOWED("40401", "您已经关注过了", HttpStatus.CONFLICT),
    NOT_FOLLOWED("40402", "您还没有关注", HttpStatus.BAD_REQUEST),
    CANNOT_FOLLOW_SELF("40403", "不能关注自己", HttpStatus.BAD_REQUEST),

    // ==================== 第三方服务错误 50000-59999 ====================
    // 文件上传 50001-50099
    FILE_UPLOAD_ERROR("50001", "文件上传失败", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_TOO_LARGE("50002", "文件过大", HttpStatus.PAYLOAD_TOO_LARGE),
    FILE_TYPE_NOT_ALLOWED("50003", "不支持的文件类型", HttpStatus.BAD_REQUEST),
    FILE_NOT_FOUND("50004", "文件不存在", HttpStatus.NOT_FOUND),

    // 消息发送 50100-50199
    SMS_SEND_ERROR("50101", "短信发送失败", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_SEND_ERROR("50102", "邮件发送失败", HttpStatus.INTERNAL_SERVER_ERROR),
    NOTIFICATION_SEND_ERROR("50103", "通知发送失败", HttpStatus.INTERNAL_SERVER_ERROR),
    VERIFICATION_CODE_SEND_ERROR("50104", "验证码发送失败", HttpStatus.INTERNAL_SERVER_ERROR),

    // 验证码相关 50200-50299
    VERIFICATION_CODE_ERROR("50101", "验证码错误", HttpStatus.BAD_REQUEST),
    VERIFICATION_CODE_EXPIRED("50202", "验证码已过期，请重新获取", HttpStatus.BAD_REQUEST),
    VERIFICATION_CODE_SEND_FREQUENT("50203", "验证码请求过于频繁，请稍后再试", HttpStatus.TOO_MANY_REQUESTS),

    // 外部API 50300-50399
    EXTERNAL_API_ERROR("50301", "外部服务调用失败", HttpStatus.BAD_GATEWAY),
    EXTERNAL_API_TIMEOUT("50302", "外部服务超时", HttpStatus.GATEWAY_TIMEOUT);

    /**
     * 错误码
     */
    private final String code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * HTTP状态码
     */
    private final HttpStatus httpStatus;

    /**
     * 根据错误码获取枚举
     *
     * @param code 错误码
     * @return ErrorCode枚举，如果找不到返回SYSTEM_ERROR
     */
    public static ErrorCode getByCode(String code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return SYSTEM_ERROR;
    }

    /**
     * 判断是否成功
     *
     * @return 是否成功
     */
    public boolean isSuccess() {
        return SUCCESS.equals(this);
    }
}