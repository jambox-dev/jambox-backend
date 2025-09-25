package org.jambox.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ApproveRequest {
    @JsonProperty("queue_id")
    private String queueId;
    private boolean approved;
}
