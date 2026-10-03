package com.telecom.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "plans")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "monthly_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyPrice;

    @Column(name = "data_allowance_gb", nullable = false)
    private int dataAllowanceGb;

    /** null = unlimited */
    @Column(name = "voice_minutes")
    private Integer voiceMinutes;

    @Column(nullable = false)
    private boolean active = true;

    protected Plan() {
    }

    public Plan(String code, String name, BigDecimal monthlyPrice, int dataAllowanceGb, Integer voiceMinutes) {
        this.code = code;
        this.name = name;
        this.monthlyPrice = monthlyPrice;
        this.dataAllowanceGb = dataAllowanceGb;
        this.voiceMinutes = voiceMinutes;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public BigDecimal getMonthlyPrice() { return monthlyPrice; }
    public int getDataAllowanceGb() { return dataAllowanceGb; }
    public Integer getVoiceMinutes() { return voiceMinutes; }
    public boolean isActive() { return active; }
}
