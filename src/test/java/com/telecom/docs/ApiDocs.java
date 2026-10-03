package com.telecom.docs;

import com.telecom.dto.CustomerResponse;
import com.telecom.dto.CustomerV2Response;
import com.telecom.dto.PlanResponse;
import com.telecom.dto.PlanSummary;
import com.telecom.entity.CustomerStatus;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import java.math.BigDecimal;
import java.time.Instant;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

/** Shared credentials, deterministic sample data and reusable field descriptors. */
public final class ApiDocs {

    public static final String AGENT = "agent";
    public static final String AGENT_PASSWORD = "agent-pass";
    public static final String ADMIN = "admin";
    public static final String ADMIN_PASSWORD = "admin-pass";

    public static final Instant CREATED = Instant.parse("2026-09-01T10:15:30Z");
    public static final Instant UPDATED = Instant.parse("2026-09-20T08:00:00Z");

    public static final PlanResponse UNLIMITED =
            new PlanResponse("UNL-599", "Unlimited 599", new BigDecimal("599.00"), 2, null);
    public static final PlanResponse DATA =
            new PlanResponse("DATA-299", "Data 299", new BigDecimal("299.00"), 1, 300);

    public static final CustomerResponse CUSTOMER = new CustomerResponse(42L, "Arun Kumar", "arun@example.com",
            "9876543210", CustomerStatus.ACTIVE, new PlanSummary("UNL-599", "Unlimited 599"), CREATED, null);

    public static final CustomerV2Response CUSTOMER_V2 = new CustomerV2Response(42L, "Arun Kumar",
            CustomerStatus.ACTIVE, new CustomerV2Response.Contact("arun@example.com", "9876543210"),
            new CustomerV2Response.Subscription("UNL-599", "Unlimited 599", new BigDecimal("599.00")), CREATED);

    private ApiDocs() {
    }

    public static FieldDescriptor[] plan() {
        return new FieldDescriptor[]{
                fieldWithPath("code").description("Unique plan code"),
                fieldWithPath("name").description("Display name"),
                fieldWithPath("monthlyPrice").description("Monthly rental in INR"),
                fieldWithPath("dataAllowanceGb").description("Daily data allowance in GB"),
                fieldWithPath("voiceMinutes").optional().type(JsonFieldType.NUMBER)
                        .description("Monthly voice minutes; absent means unlimited")};
    }

    public static FieldDescriptor[] customer() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("Customer id"),
                fieldWithPath("name").description("Full name"),
                fieldWithPath("email").description("Email address"),
                fieldWithPath("msisdn").description("10-digit mobile number"),
                fieldWithPath("status").description("`ACTIVE`, `SUSPENDED` or `TERMINATED`"),
                fieldWithPath("plan").optional().type(JsonFieldType.OBJECT).description("Current plan; absent if none"),
                fieldWithPath("plan.code").optional().type(JsonFieldType.STRING).description("Plan code"),
                fieldWithPath("plan.name").optional().type(JsonFieldType.STRING).description("Plan name"),
                fieldWithPath("createdAt").description("Creation time (ISO-8601, UTC)"),
                fieldWithPath("updatedAt").optional().type(JsonFieldType.STRING)
                        .description("Last update time (ISO-8601, UTC); absent if never updated")};
    }

    public static FieldDescriptor[] page() {
        return new FieldDescriptor[]{
                fieldWithPath("content[]").description("Items on this page"),
                fieldWithPath("page").description("Zero-based page number"),
                fieldWithPath("size").description("Requested page size (max 100)"),
                fieldWithPath("totalElements").description("Total number of matching items"),
                fieldWithPath("totalPages").description("Total number of pages")};
    }

    public static FieldDescriptor[] problem() {
        return new FieldDescriptor[]{
                fieldWithPath("type").description("Problem type URI (`about:blank` for plain HTTP errors)"),
                fieldWithPath("title").description("Short summary of the problem"),
                fieldWithPath("status").description("HTTP status code"),
                fieldWithPath("detail").description("Human-readable explanation"),
                fieldWithPath("instance").description("Path of the request that failed")};
    }
}
