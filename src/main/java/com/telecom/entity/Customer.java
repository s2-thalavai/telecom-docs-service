package com.telecom.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, unique = true, length = 15)
    private String msisdn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CustomerStatus status = CustomerStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    private long version;

    protected Customer() {
    }

    public Customer(String name, String email, String msisdn, Plan plan) {
        this.name = name;
        this.email = email;
        this.msisdn = msisdn;
        this.plan = plan;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void update(String name, String email, String msisdn, Plan plan) {
        this.name = name;
        this.email = email;
        this.msisdn = msisdn;
        this.plan = plan;
    }

    public void changeStatus(CustomerStatus status) {
        this.status = status;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getMsisdn() { return msisdn; }
    public CustomerStatus getStatus() { return status; }
    public Plan getPlan() { return plan; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
