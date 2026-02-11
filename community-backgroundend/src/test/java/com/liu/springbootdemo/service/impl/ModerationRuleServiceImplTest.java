package com.liu.springbootdemo.service.impl;

import com.github.pagehelper.Page;
import com.liu.springbootdemo.POJO.Result.PageResult;
import com.liu.springbootdemo.POJO.entity.ModerationRule;
import com.liu.springbootdemo.POJO.entity.User;
import com.liu.springbootdemo.POJO.vo.ModerationRuleVO;
import com.liu.springbootdemo.common.enums.ErrorCode;
import com.liu.springbootdemo.common.exception.BusinessException;
import com.liu.springbootdemo.common.utils.SecurityUtil;
import com.liu.springbootdemo.mapper.ModerationRuleMapper;
import com.liu.springbootdemo.service.ModerationAuditLogService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 审核规则管理服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class ModerationRuleServiceImplTest {

    @Mock
    private ModerationRuleMapper moderationRuleMapper;

    @Mock
    private ModerationAuditLogService auditLogService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private ModerationRuleServiceImpl moderationRuleService;

    // ============ 分页查询测试 ============

    @Nested
    class PageQueryTests {

        @Test
        void pageQuery_noFilter_returnsAll() {
            Page<ModerationRuleVO> mockPage = new Page<>(1, 10);
            mockPage.setTotal(2);
            mockPage.add(createRuleVO(1L, "keyword", "赌博", 80));
            mockPage.add(createRuleVO(2L, "regex", "v信.*", 60));
            when(moderationRuleMapper.pageQuery(isNull(), isNull())).thenReturn(mockPage);

            PageResult result = moderationRuleService.pageQuery(1, 10, null, null);
            assertEquals(2, result.getTotal());
            assertEquals(2, result.getResults().size());
        }

        @Test
        void pageQuery_byRuleType_returnsFiltered() {
            Page<ModerationRuleVO> mockPage = new Page<>(1, 10);
            mockPage.setTotal(1);
            mockPage.add(createRuleVO(1L, "keyword", "赌博", 80));
            when(moderationRuleMapper.pageQuery(eq("keyword"), isNull())).thenReturn(mockPage);

            PageResult result = moderationRuleService.pageQuery(1, 10, "keyword", null);
            assertEquals(1, result.getTotal());
        }

        @Test
        void pageQuery_byEnabled_returnsFiltered() {
            Page<ModerationRuleVO> mockPage = new Page<>(1, 10);
            mockPage.setTotal(1);
            mockPage.add(createRuleVO(1L, "keyword", "赌博", 80));
            when(moderationRuleMapper.pageQuery(isNull(), eq(true))).thenReturn(mockPage);

            PageResult result = moderationRuleService.pageQuery(1, 10, null, true);
            assertEquals(1, result.getTotal());
        }
    }

    // ============ 新增规则测试 ============

    @Nested
    class CreateRuleTests {

        @Test
        void createRule_keyword_success() {
            ModerationRule savedRule = createRule(1L, "keyword", "赌博", 80, true);
            when(moderationRuleMapper.findById(anyLong())).thenReturn(savedRule);
            doAnswer(inv -> {
                ModerationRule rule = inv.getArgument(0);
                rule.setId(1L);
                return null;
            }).when(moderationRuleMapper).insert(any(ModerationRule.class));

            try (MockedStatic<SecurityUtil> securityMock = mockStatic(SecurityUtil.class)) {
                securityMock.when(SecurityUtil::getCurrentUser).thenReturn(createAdminUser());

                ModerationRuleVO vo = moderationRuleService.createRule("keyword", "赌博", 80, true, "赌博关键词");
                assertNotNull(vo);
                assertEquals("keyword", vo.getRuleType());
                assertEquals("赌博", vo.getPattern());
                assertEquals(80, vo.getWeightScore());
                verify(moderationRuleMapper).insert(any(ModerationRule.class));
                verify(auditLogService).logManualAction(eq(1L), eq("rule"), eq("rule_create"),
                        anyLong(), anyString(), anyString(), isNull(), eq(0));
            }
        }

        @Test
        void createRule_regex_success() {
            ModerationRule savedRule = createRule(2L, "regex", "v信.*", 60, true);
            when(moderationRuleMapper.findById(anyLong())).thenReturn(savedRule);
            doAnswer(inv -> {
                ModerationRule rule = inv.getArgument(0);
                rule.setId(2L);
                return null;
            }).when(moderationRuleMapper).insert(any(ModerationRule.class));

            try (MockedStatic<SecurityUtil> securityMock = mockStatic(SecurityUtil.class)) {
                securityMock.when(SecurityUtil::getCurrentUser).thenReturn(createAdminUser());

                ModerationRuleVO vo = moderationRuleService.createRule("regex", "v信.*", 60, true, "微信引流");
                assertNotNull(vo);
                assertEquals("regex", vo.getRuleType());
            }
        }

        @Test
        void createRule_blacklist_success() {
            ModerationRule savedRule = createRule(3L, "blacklist", "evil.com", 40, true);
            when(moderationRuleMapper.findById(anyLong())).thenReturn(savedRule);
            doAnswer(inv -> {
                ModerationRule rule = inv.getArgument(0);
                rule.setId(3L);
                return null;
            }).when(moderationRuleMapper).insert(any(ModerationRule.class));

            try (MockedStatic<SecurityUtil> securityMock = mockStatic(SecurityUtil.class)) {
                securityMock.when(SecurityUtil::getCurrentUser).thenReturn(createAdminUser());

                ModerationRuleVO vo = moderationRuleService.createRule("blacklist", "evil.com", 40, true, "恶意域名");
                assertNotNull(vo);
                assertEquals("blacklist", vo.getRuleType());
            }
        }

        @Test
        void createRule_invalidRuleType_throwsException() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> moderationRuleService.createRule("invalid", "test", 50, true, ""));
            assertEquals(ErrorCode.PARAM_ERROR.getCode(), ex.getCode());
            assertTrue(ex.getMessage().contains("非法规则类型"));
        }

        @Test
        void createRule_refreshesCacheOnSuccess() {
            ModerationRule savedRule = createRule(1L, "keyword", "test", 50, true);
            when(moderationRuleMapper.findById(anyLong())).thenReturn(savedRule);
            doAnswer(inv -> {
                ModerationRule rule = inv.getArgument(0);
                rule.setId(1L);
                return null;
            }).when(moderationRuleMapper).insert(any(ModerationRule.class));

            try (MockedStatic<SecurityUtil> securityMock = mockStatic(SecurityUtil.class)) {
                securityMock.when(SecurityUtil::getCurrentUser).thenReturn(createAdminUser());

                moderationRuleService.createRule("keyword", "test", 50, true, "");
                verify(redisTemplate).delete("moderation:rules:enabled");
            }
        }

        @Test
        void createRule_cacheRefreshFailure_doesNotAffectResult() {
            ModerationRule savedRule = createRule(1L, "keyword", "test", 50, true);
            when(moderationRuleMapper.findById(anyLong())).thenReturn(savedRule);
            doAnswer(inv -> {
                ModerationRule rule = inv.getArgument(0);
                rule.setId(1L);
                return null;
            }).when(moderationRuleMapper).insert(any(ModerationRule.class));
            when(redisTemplate.delete(anyString())).thenThrow(new RuntimeException("Redis down"));

            try (MockedStatic<SecurityUtil> securityMock = mockStatic(SecurityUtil.class)) {
                securityMock.when(SecurityUtil::getCurrentUser).thenReturn(createAdminUser());

                ModerationRuleVO vo = moderationRuleService.createRule("keyword", "test", 50, true, "");
                assertNotNull(vo);
            }
        }
    }

    // ============ 修改规则测试 ============

    @Nested
    class UpdateRuleTests {

        @Test
        void updateRule_success() {
            ModerationRule existing = createRule(1L, "keyword", "赌博", 80, true);
            ModerationRule updated = createRule(1L, "keyword", "赌博网站", 90, true);
            when(moderationRuleMapper.findById(1L)).thenReturn(existing).thenReturn(updated);

            try (MockedStatic<SecurityUtil> securityMock = mockStatic(SecurityUtil.class)) {
                securityMock.when(SecurityUtil::getCurrentUser).thenReturn(createAdminUser());

                ModerationRuleVO vo = moderationRuleService.updateRule(1L, null, "赌博网站", 90, null, null);
                assertNotNull(vo);
                assertEquals("赌博网站", vo.getPattern());
                assertEquals(90, vo.getWeightScore());
                verify(moderationRuleMapper).update(any(ModerationRule.class));
                verify(redisTemplate).delete("moderation:rules:enabled");
            }
        }

        @Test
        void updateRule_changeRuleType_success() {
            ModerationRule existing = createRule(1L, "keyword", "test", 50, true);
            ModerationRule updated = createRule(1L, "regex", "test.*", 50, true);
            when(moderationRuleMapper.findById(1L)).thenReturn(existing).thenReturn(updated);

            try (MockedStatic<SecurityUtil> securityMock = mockStatic(SecurityUtil.class)) {
                securityMock.when(SecurityUtil::getCurrentUser).thenReturn(createAdminUser());

                ModerationRuleVO vo = moderationRuleService.updateRule(1L, "regex", "test.*", null, null, null);
                assertEquals("regex", vo.getRuleType());
            }
        }

        @Test
        void updateRule_disableRule_success() {
            ModerationRule existing = createRule(1L, "keyword", "test", 50, true);
            ModerationRule updated = createRule(1L, "keyword", "test", 50, false);
            when(moderationRuleMapper.findById(1L)).thenReturn(existing).thenReturn(updated);

            try (MockedStatic<SecurityUtil> securityMock = mockStatic(SecurityUtil.class)) {
                securityMock.when(SecurityUtil::getCurrentUser).thenReturn(createAdminUser());

                ModerationRuleVO vo = moderationRuleService.updateRule(1L, null, null, null, false, null);
                assertFalse(vo.isEnabled());
            }
        }

        @Test
        void updateRule_notFound_throwsException() {
            when(moderationRuleMapper.findById(999L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> moderationRuleService.updateRule(999L, null, "test", null, null, null));
            assertEquals(ErrorCode.PARAM_ERROR.getCode(), ex.getCode());
            assertTrue(ex.getMessage().contains("规则不存在"));
        }

        @Test
        void updateRule_invalidRuleType_throwsException() {
            ModerationRule existing = createRule(1L, "keyword", "test", 50, true);
            when(moderationRuleMapper.findById(1L)).thenReturn(existing);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> moderationRuleService.updateRule(1L, "invalid", null, null, null, null));
            assertEquals(ErrorCode.PARAM_ERROR.getCode(), ex.getCode());
        }
    }

    // ============ 删除规则测试 ============

    @Nested
    class DeleteRuleTests {

        @Test
        void deleteRule_success() {
            ModerationRule existing = createRule(1L, "keyword", "赌博", 80, true);
            when(moderationRuleMapper.findById(1L)).thenReturn(existing);
            when(moderationRuleMapper.softDelete(1L)).thenReturn(1);

            try (MockedStatic<SecurityUtil> securityMock = mockStatic(SecurityUtil.class)) {
                securityMock.when(SecurityUtil::getCurrentUser).thenReturn(createAdminUser());

                assertDoesNotThrow(() -> moderationRuleService.deleteRule(1L));
                verify(moderationRuleMapper).softDelete(1L);
                verify(redisTemplate).delete("moderation:rules:enabled");
                verify(auditLogService).logManualAction(eq(1L), eq("rule"), eq("rule_delete"),
                        anyLong(), anyString(), anyString(), isNull(), eq(0));
            }
        }

        @Test
        void deleteRule_notFound_throwsException() {
            when(moderationRuleMapper.findById(999L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> moderationRuleService.deleteRule(999L));
            assertEquals(ErrorCode.PARAM_ERROR.getCode(), ex.getCode());
        }

        @Test
        void deleteRule_alreadyDeleted_throwsException() {
            ModerationRule existing = createRule(1L, "keyword", "test", 50, true);
            when(moderationRuleMapper.findById(1L)).thenReturn(existing);
            when(moderationRuleMapper.softDelete(1L)).thenReturn(0);

            try (MockedStatic<SecurityUtil> securityMock = mockStatic(SecurityUtil.class)) {
                securityMock.when(SecurityUtil::getCurrentUser).thenReturn(null);

                BusinessException ex = assertThrows(BusinessException.class,
                        () -> moderationRuleService.deleteRule(1L));
                assertTrue(ex.getMessage().contains("规则不存在或已删除"));
            }
        }
    }

    // ============ 审计日志写入测试 ============

    @Nested
    class AuditLogTests {

        @Test
        void createRule_writesAuditLog() {
            ModerationRule savedRule = createRule(1L, "keyword", "test", 50, true);
            when(moderationRuleMapper.findById(anyLong())).thenReturn(savedRule);
            doAnswer(inv -> {
                ModerationRule rule = inv.getArgument(0);
                rule.setId(1L);
                return null;
            }).when(moderationRuleMapper).insert(any(ModerationRule.class));

            try (MockedStatic<SecurityUtil> securityMock = mockStatic(SecurityUtil.class)) {
                User admin = createAdminUser();
                securityMock.when(SecurityUtil::getCurrentUser).thenReturn(admin);

                moderationRuleService.createRule("keyword", "test", 50, true, "");
                verify(auditLogService).logManualAction(
                        eq(1L), eq("rule"), eq("rule_create"),
                        eq(admin.getId()), eq(admin.getUsername()),
                        contains("新增规则"), isNull(), eq(0));
            }
        }

        @Test
        void updateRule_writesAuditLog() {
            ModerationRule existing = createRule(1L, "keyword", "test", 50, true);
            when(moderationRuleMapper.findById(1L)).thenReturn(existing);

            try (MockedStatic<SecurityUtil> securityMock = mockStatic(SecurityUtil.class)) {
                securityMock.when(SecurityUtil::getCurrentUser).thenReturn(createAdminUser());

                moderationRuleService.updateRule(1L, null, "updated", null, null, null);
                verify(auditLogService).logManualAction(
                        eq(1L), eq("rule"), eq("rule_update"),
                        anyLong(), anyString(), contains("更新规则"), isNull(), eq(0));
            }
        }

        @Test
        void deleteRule_writesAuditLog() {
            ModerationRule existing = createRule(1L, "keyword", "赌博", 80, true);
            when(moderationRuleMapper.findById(1L)).thenReturn(existing);
            when(moderationRuleMapper.softDelete(1L)).thenReturn(1);

            try (MockedStatic<SecurityUtil> securityMock = mockStatic(SecurityUtil.class)) {
                securityMock.when(SecurityUtil::getCurrentUser).thenReturn(createAdminUser());

                moderationRuleService.deleteRule(1L);
                verify(auditLogService).logManualAction(
                        eq(1L), eq("rule"), eq("rule_delete"),
                        anyLong(), anyString(), contains("删除规则"), isNull(), eq(0));
            }
        }
    }

    // ============ 辅助方法 ============

    private ModerationRule createRule(Long id, String ruleType, String pattern, int weightScore, boolean enabled) {
        ModerationRule rule = new ModerationRule();
        rule.setId(id);
        rule.setRuleType(ruleType);
        rule.setPattern(pattern);
        rule.setWeightScore(weightScore);
        rule.setEnabled(enabled);
        rule.setDescription("测试规则");
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());
        return rule;
    }

    private ModerationRuleVO createRuleVO(Long id, String ruleType, String pattern, int weightScore) {
        return new ModerationRuleVO(id, ruleType, pattern, weightScore, true,
                "测试规则", LocalDateTime.now(), LocalDateTime.now());
    }

    private User createAdminUser() {
        User user = new User();
        user.setId(100L);
        user.setUsername("admin");
        return user;
    }
}
