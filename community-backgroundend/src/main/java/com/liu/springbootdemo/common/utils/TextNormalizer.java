package com.liu.springbootdemo.common.utils;

/**
 * 文本预处理工具类
 * 用于审核规则匹配前的文本归一化处理
 */
public class TextNormalizer {

    private TextNormalizer() {}

    /**
     * 对输入文本进行归一化预处理：
     * 1. 去除多余空格（连续空格合并为一个）
     * 2. 统一转为小写
     * 3. 全角字符转半角
     *
     * @param text 原始文本
     * @return 归一化后的文本
     */
    public static String normalize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        // 1. 全角转半角
        String result = fullWidthToHalfWidth(text);
        // 2. 统一小写
        result = result.toLowerCase();
        // 3. 去除多余空格（连续空格合并为一个，首尾去除）
        result = result.replaceAll("\\s+", " ").trim();
        return result;
    }

    /**
     * 全角字符转半角字符
     * 全角空格（\u3000）→ 半角空格
     * 全角字符 \uFF01 ~ \uFF5E → 对应半角 \u0021 ~ \u007E
     */
    private static String fullWidthToHalfWidth(String text) {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '\u3000') {
                // 全角空格转半角空格
                chars[i] = ' ';
            } else if (chars[i] >= '\uFF01' && chars[i] <= '\uFF5E') {
                // 全角字符转半角：偏移量 0xFEE0
                chars[i] = (char) (chars[i] - 0xFEE0);
            }
        }
        return new String(chars);
    }
}
