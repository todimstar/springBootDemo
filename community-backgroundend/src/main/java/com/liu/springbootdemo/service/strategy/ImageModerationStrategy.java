package com.liu.springbootdemo.service.strategy;

import com.liu.springbootdemo.POJO.dto.RiskScoreResult;
import com.liu.springbootdemo.POJO.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 图片内容审核策略（占位实现）
 * <p>
 * 当前版本仅预留扩展点，不执行实际的图片审核逻辑。
 * 后续可接入第三方图片审核 API 或本地模型。
 */
@Component
@Slf4j
public class ImageModerationStrategy implements ContentModerationStrategy {

    public static final String CONTENT_TYPE = "image";

    @Override
    public boolean supports(String contentType) {
        return CONTENT_TYPE.equalsIgnoreCase(contentType);
    }

    @Override
    public RiskScoreResult evaluate(String content, User user) {
        log.info("图片审核策略暂未实现，内容直接放行: contentLength={}", content != null ? content.length() : 0);
        return RiskScoreResult.safe();
    }
}
