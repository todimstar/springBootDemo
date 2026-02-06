package com.liu.springbootdemo.common.utils;

import com.liu.springbootdemo.common.enums.ErrorCode;
import com.liu.springbootdemo.common.exception.BusinessException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Map;

/**
 * 断言工具类
 * 简化参数校验和业务规则校验
 *
 * @author Liu
 * @date 2025
 */
public class Assert {

    /**
     * 断言对象不为null
     *
     * @param object    要检查的对象
     * @param errorCode 错误码
     */
    public static void notNull(Object object, ErrorCode errorCode) {
        if (object == null) {
            throw new BusinessException(errorCode);
        }
    }

    /**
     * 断言对象不为null（自定义消息）
     *
     * @param object    要检查的对象
     * @param errorCode 错误码
     * @param message   自定义错误消息
     */
    public static void notNull(Object object, ErrorCode errorCode, String message) {
        if (object == null) {
            throw new BusinessException(errorCode, message);
        }
    }

    /**
     * 断言对象为null
     *
     * @param object    要检查的对象
     * @param errorCode 错误码
     */
    public static void isNull(Object object, ErrorCode errorCode) {
        if (object != null) {
            throw new BusinessException(errorCode);
        }
    }

    /**
     * 断言表达式为true
     *
     * @param expression 表达式
     * @param errorCode  错误码
     */
    public static void isTrue(boolean expression, ErrorCode errorCode) {
        if (!expression) {
            throw new BusinessException(errorCode);
        }
    }

    /**
     * 断言表达式为true（自定义消息）
     *
     * @param expression 表达式
     * @param errorCode  错误码
     * @param message    自定义错误消息
     */
    public static void isTrue(boolean expression, ErrorCode errorCode, String message) {
        if (!expression) {
            throw new BusinessException(errorCode, message);
        }
    }

    /**
     * 断言表达式为false
     *
     * @param expression 表达式
     * @param errorCode  错误码
     */
    public static void isFalse(boolean expression, ErrorCode errorCode) {
        if (expression) {
            throw new BusinessException(errorCode);
        }
    }

    /**
     * 断言字符串不为空
     *
     * @param text      要检查的字符串
     * @param errorCode 错误码
     */
    public static void hasText(String text, ErrorCode errorCode) {
        if (!StringUtils.hasText(text)) {
            throw new BusinessException(errorCode);
        }
    }

    /**
     * 断言字符串不为空（自定义消息）
     *
     * @param text      要检查的字符串
     * @param errorCode 错误码
     * @param message   自定义错误消息
     */
    public static void hasText(String text, ErrorCode errorCode, String message) {
        if (!StringUtils.hasText(text)) {
            throw new BusinessException(errorCode, message);
        }
    }

    /**
     * 断言字符串长度在指定范围内
     *
     * @param text      要检查的字符串
     * @param min       最小长度
     * @param max       最大长度
     * @param errorCode 错误码
     */
    public static void lengthBetween(String text, int min, int max, ErrorCode errorCode) {
        if (text == null || text.length() < min || text.length() > max) {
            throw new BusinessException(errorCode,
                    String.format("长度必须在 %d 到 %d 之间", min, max));
        }
    }

    /**
     * 断言集合不为空
     *
     * @param collection 要检查的集合
     * @param errorCode  错误码
     */
    public static void notEmpty(Collection<?> collection, ErrorCode errorCode) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new BusinessException(errorCode);
        }
    }

    /**
     * 断言Map不为空
     *
     * @param map       要检查的Map
     * @param errorCode 错误码
     */
    public static void notEmpty(Map<?, ?> map, ErrorCode errorCode) {
        if (CollectionUtils.isEmpty(map)) {
            throw new BusinessException(errorCode);
        }
    }

    /**
     * 断言数组不为空
     *
     * @param array     要检查的数组
     * @param errorCode 错误码
     */
    public static void notEmpty(Object[] array, ErrorCode errorCode) {
        if (array == null || array.length == 0) {
            throw new BusinessException(errorCode);
        }
    }

    /**
     * 断言两个对象相等
     *
     * @param expected  期望值
     * @param actual    实际值
     * @param errorCode 错误码
     */
    public static void equals(Object expected, Object actual, ErrorCode errorCode) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new BusinessException(errorCode);
        }
    }

    /**
     * 断言两个对象不相等
     *
     * @param obj1      对象1
     * @param obj2      对象2
     * @param errorCode 错误码
     */
    public static void notEquals(Object obj1, Object obj2, ErrorCode errorCode) {
        if (obj1 == null ? obj2 == null : obj1.equals(obj2)) {
            throw new BusinessException(errorCode);
        }
    }

    /**
     * 断言数字在指定范围内
     *
     * @param value     要检查的值
     * @param min       最小值（包含）
     * @param max       最大值（包含）
     * @param errorCode 错误码
     */
    public static void between(long value, long min, long max, ErrorCode errorCode) {
        if (value < min || value > max) {
            throw new BusinessException(errorCode,
                    String.format("值必须在 %d 到 %d 之间", min, max));
        }
    }

    /**
     * 断言数字大于指定值
     *
     * @param value     要检查的值
     * @param min       最小值（不包含）
     * @param errorCode 错误码
     */
    public static void greaterThan(long value, long min, ErrorCode errorCode) {
        if (value <= min) {
            throw new BusinessException(errorCode,
                    String.format("值必须大于 %d", min));
        }
    }

    /**
     * 断言数字小于指定值
     *
     * @param value     要检查的值
     * @param max       最大值（不包含）
     * @param errorCode 错误码
     */
    public static void lessThan(long value, long max, ErrorCode errorCode) {
        if (value >= max) {
            throw new BusinessException(errorCode,
                    String.format("值必须小于 %d", max));
        }
    }

    /**
     * 断言邮箱格式正确
     *
     * @param email     邮箱地址
     * @param errorCode 错误码
     */
    public static void isEmail(String email, ErrorCode errorCode) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (email == null || !email.matches(emailRegex)) {
            throw new BusinessException(errorCode, "邮箱格式不正确");
        }
    }

    /**
     * 断言手机号格式正确（中国大陆）
     *
     * @param phone     手机号
     * @param errorCode 错误码
     */
    public static void isPhone(String phone, ErrorCode errorCode) {
        String phoneRegex = "^1[3-9]\\d{9}$";
        if (phone == null || !phone.matches(phoneRegex)) {
            throw new BusinessException(errorCode, "手机号格式不正确");
        }
    }
}