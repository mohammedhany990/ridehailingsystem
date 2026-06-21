package com.ridehailing.userservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatusResponse {
    private String key;
    private String label;
}
