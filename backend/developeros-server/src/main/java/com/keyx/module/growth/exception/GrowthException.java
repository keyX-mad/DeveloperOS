package com.keyx.module.growth.exception;

import com.keyx.common.exception.BusinessException;
import lombok.Getter;

/**
 * Growth 模块业务异常
 *
 * 继承 BusinessException，错误码用 1700+ 段
 * 错误码规划：
 *   1700 = 通用 Growth 错误
 *   1701 = 目标不存在
 *   1702 = 任务不存在
 *   1703 = 日志不存在
 *   1704 = 进度重算失败
 *   1705 = 状态流转非法
 *
 * 使用示例：
 * <pre>
 *   throw new GrowthException.goalNotFound();
 *   throw new GrowthException(1705, "不能从 completed 流转到 active");
 * </pre>
 */
@Getter
public class GrowthException extends BusinessException {

  public GrowthException(String message) {
    super(message);
  }

  public GrowthException(Integer code, String message) {
    super(code, message);
  }

  public static GrowthException goalNotFound() {
    return new GrowthException(1701, "学习目标不存在");
  }

  public static GrowthException taskNotFound() {
    return new GrowthException(1702, "学习任务不存在");
  }

  public static GrowthException logNotFound() {
    return new GrowthException(1703, "成长日志不存在");
  }

  public static GrowthException invalidStatusTransition(String detail) {
    return new GrowthException(1705, "状态流转非法：" + detail);
  }
}