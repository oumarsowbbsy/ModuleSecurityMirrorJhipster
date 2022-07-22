package io.onboard.userservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.elasticsearch.annotations.Field;

import java.io.Serializable;
import java.time.Instant;

public class AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Field("created_by")
    @JsonIgnore
    private String createdBy;

    @CreatedDate
    @Field("createded_date")
    @JsonIgnore
    private Instant createdDate = Instant.now();

    @LastModifiedDate
    @Field("last_modified_date")
    @JsonIgnore
    private Instant lastModifiedDate = Instant.now();

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}
