package com.liu.springbootdemo.service.impl;

import com.liu.springbootdemo.POJO.entity.ModerationAuditLog;
import com.liu.springbootdemo.POJO.vo.ModerationAuditLogVO;
import com.liu.springbootdemo.mapper.ModerationAuditLogMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 审核审计日志服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class ModerationAuditLogServiceImplTest {

    @Mock
    private ModerationAuditLogMapper auditLogMapper;

    @InjectMocks
    private ModerationAuditLogServiceImpl auditLogService;

    // ============ 自动审核日志测试 ============

    @Test
    void logAutoAction_autoApprove_writesSystemLog() {
        auditLogService.logAutoAction(100L, "post", "auto_approve", "[]", 10);

        ArgumentCaptor<ModerationAuditLog> captor = ArgumentCaptor.forClass(ModerationAuditLog.class);
        verify(auditLogMapper).insert(captor.capture());

        ModerationAuditLog log = captor.getValue();
        assertEquals(100L, log.getTargetId());
        assertEquals("post", log.getTargetType());
        assertEquals("auto_approve", log.getActionType());
        assertNull(log.getOperatorId());
        assertEquals("SYSTEM", log.getOperatorName());
        assertNull(log.getReason());
        assertEquals("[]", log.getHitRules());
        assertEquals(10, log.getRiskScore());
    }

    @Test
    void logAutoAction_autoReject_writesSystemLog() {
        String hitRulesJson = "[{\"ruleId\":1,\"ruleType\":\"keyword\",\"matchedContent\":\"赌博\",\"weightScore\":80}]";
        auditLogService.logAutoAction(200L, "comment", "auto_reject", hitRulesJson, 85);

        ArgumentCaptor<ModerationAuditLog> captor = ArgumentCaptor.forClass(ModerationAuditLog.class);
        verify(auditLogMapper).insert(captor.capture());

        ModerationAuditLog log = captor.getValue();
        assertEquals(200L, log.getTargetId());
        assertEquals("comment", log.getTargetType());
        assertEquals("auto_reject", log.getActionType());
        assertNull(log.getOperatorId());
        assertEquals("SYSTEM", log.getOperatorName());
        assertEquals(85, log.getRiskScore());
        assertEquals(hitRulesJson, log.getHitRules());
    }

    @Test
    void logAutoAction_pendingReview_writesSystemLog() {
        auditLogService.logAutoAction(300L, "post", "pending_review", "[]", 50);

        ArgumentCaptor<ModerationAuditLog> captor = ArgumentCaptor.forClass(ModerationAuditLog.class);
        verify(auditLogMapper).insert(captor.capture());

        ModerationAuditLog log = captor.getValue();
        assertEquals("pending_review", log.getActionType());
        assertEquals("SYSTEM", log.getOperatorName());
        assertEquals(50, log.getRiskScore());
    }

    @Test
    void logAutoAction_insertFails_doesNotThrow() {
        doThrow(new RuntimeException("DB error")).when(auditLogMapper).insert(any());

        // 不应抛出异常
        assertDoesNotThrow(() ->
                auditLogService.logAutoAction(100L, "post", "auto_approve", "[]", 10));
    }

    // ============ 人工审核日志测试 ============

    @Test
    void logManualAction_approve_writesOperatorLog() {
        String hitRules = "[{\"ruleId\":1}]";
        auditLogService.logManualAction(100L, "post", "approve",
                10L, "admin_user", "内容正常", hitRules, 50);

        ArgumentCaptor<ModerationAuditLog> captor = ArgumentCaptor.forClass(ModerationAuditLog.class);
        verify(auditLogMapper).insert(captor.capture());

        ModerationAuditLog log = captor.getValue();
        assertEquals(100L, log.getTargetId());
        assertEquals("post", log.getTargetType());
        assertEquals("approve", log.getActionType());
        assertEquals(10L, log.getOperatorId());
        assertEquals("admin_user", log.getOperatorName());
        assertEquals("内容正常", log.getReason());
        assertEquals(hitRules, log.getHitRules());
        assertEquals(50, log.getRiskScore());
    }

    @Test
    void logManualAction_reject_writesOperatorLog() {
        auditLogService.logManualAction(200L, "comment", "reject",
                20L, "moderator", "违规内容", "[]", 65);

        ArgumentCaptor<ModerationAuditLog> captor = ArgumentCaptor.forClass(ModerationAuditLog.class);
        verify(auditLogMapper).insert(captor.capture());

        ModerationAuditLog log = captor.getValue();
        assertEquals("reject", log.getActionType());
        assertEquals(20L, log.getOperatorId());
        assertEquals("moderator", log.getOperatorName());
        assertEquals("违规内容", log.getReason());
    }

    @Test
    void logManualAction_shadowBan_writesOperatorLog() {
        auditLogService.logManualAction(300L, "post", "shadow_ban",
                10L, "admin", "隐蔽处理", "[]", 55);

        ArgumentCaptor<ModerationAuditLog> captor = ArgumentCaptor.forClass(ModerationAuditLog.class);
        verify(auditLogMapper).insert(captor.capture());

        ModerationAuditLog log = captor.getValue();
        assertEquals("shadow_ban", log.getActionType());
        assertEquals("隐蔽处理", log.getReason());
    }

    @Test
    void logManualAction_insertFails_doesNotThrow() {
        doThrow(new RuntimeException("DB error")).when(auditLogMapper).insert(any());

        assertDoesNotThrow(() ->
                auditLogService.logManualAction(100L, "post", "approve",
                        10L, "admin", "通过", "[]", 50));
    }

    // ============ 审计日志查询测试 ============

    @Test
    void getAuditLogs_hasLogs_returnsInOrder() {
        ModerationAuditLogVO log1 = createLogVO(1L, "auto_approve", "SYSTEM", null);
        ModerationAuditLogVO log2 = createLogVO(2L, "reject", "admin", "违规内容");
        when(auditLogMapper.findByTarget(100L, "post")).thenReturn(Arrays.asList(log1, log2));

        List<ModerationAuditLogVO> result = auditLogService.getAuditLogs(100L, "post");

        assertEquals(2, result.size());
        assertEquals("auto_approve", result.get(0).getActionType());
        assertEquals("SYSTEM", result.get(0).getOperatorName());
        assertEquals("reject", result.get(1).getActionType());
        assertEquals("admin", result.get(1).getOperatorName());
        assertEquals("违规内容", result.get(1).getReason());
    }

    @Test
    void getAuditLogs_noLogs_returnsEmptyList() {
        when(auditLogMapper.findByTarget(999L, "post")).thenReturn(Collections.emptyList());

        List<ModerationAuditLogVO> result = auditLogService.getAuditLogs(999L, "post");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAuditLogs_commentType_returnsCommentLogs() {
        ModerationAuditLogVO log1 = createLogVO(1L, "pending_review", "SYSTEM", null);
        log1.setTargetType("comment");
        log1.setTargetId(200L);
        when(auditLogMapper.findByTarget(200L, "comment")).thenReturn(Collections.singletonList(log1));

        List<ModerationAuditLogVO> result = auditLogService.getAuditLogs(200L, "comment");

        assertEquals(1, result.size());
        assertEquals("comment", result.get(0).getTargetType());
    }

    // ============ 辅助方法 ============

    private ModerationAuditLogVO createLogVO(Long id, String actionType, String operatorName, String reason) {
        ModerationAuditLogVO vo = new ModerationAuditLogVO();
        vo.setId(id);
        vo.setTargetId(100L);
        vo.setTargetType("post");
        vo.setActionType(actionType);
        vo.setOperatorId("SYSTEM".equals(operatorName) ? null : 10L);
        vo.setOperatorName(operatorName);
        vo.setReason(reason);
        vo.setHitRules("[]");
        vo.setRiskScore(50);
        vo.setCreatedAt(LocalDateTime.now());
        return vo;
    }
}
