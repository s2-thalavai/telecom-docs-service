package com.telecom.dto;

import java.math.BigDecimal;

public record PlanResponse(String code, String name, BigDecimal monthlyPrice, int dataAllowanceGb,
                           Integer voiceMinutes) {
}
