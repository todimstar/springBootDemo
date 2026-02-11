package com.liu.springbootdemo.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TextNormalizer 单元测试
 */
class TextNormalizerTest {

    @Test
    void normalize_null_returnsEmpty() {
        assertEquals("", TextNormalizer.normalize(null));
    }

    @Test
    void normalize_empty_returnsEmpty() {
        assertEquals("", TextNormalizer.normalize(""));
    }

    @Test
    void normalize_multipleSpaces_collapsedToOne() {
        assertEquals("hello world", TextNormalizer.normalize("hello   world"));
    }

    @Test
    void normalize_leadingTrailingSpaces_trimmed() {
        assertEquals("hello", TextNormalizer.normalize("  hello  "));
    }

    @Test
    void normalize_uppercase_convertedToLowercase() {
        assertEquals("hello world", TextNormalizer.normalize("HELLO WORLD"));
    }

    @Test
    void normalize_fullWidthLetters_convertedToHalfWidth() {
        // 全角字母 ＡＢＣ → abc
        assertEquals("abc", TextNormalizer.normalize("\uFF21\uFF22\uFF23"));
    }

    @Test
    void normalize_fullWidthDigits_convertedToHalfWidth() {
        // 全角数字 １２３ → 123
        assertEquals("123", TextNormalizer.normalize("\uFF11\uFF12\uFF13"));
    }

    @Test
    void normalize_fullWidthSpace_convertedToHalfWidth() {
        // 全角空格 \u3000
        assertEquals("a b", TextNormalizer.normalize("a\u3000b"));
    }

    @Test
    void normalize_mixedContent_allNormalized() {
        // 混合：全角、大写、多余空格
        String input = "Ｈello  ＷORLD　ＡＢＣ";
        String expected = "hello world abc";
        assertEquals(expected, TextNormalizer.normalize(input));
    }

    @Test
    void normalize_chineseText_preserved() {
        assertEquals("你好世界", TextNormalizer.normalize("你好世界"));
    }

    @Test
    void normalize_fullWidthPunctuation_convertedToHalfWidth() {
        // 全角冒号 ： → :
        assertEquals("key:value", TextNormalizer.normalize("key：value"));
    }
}
