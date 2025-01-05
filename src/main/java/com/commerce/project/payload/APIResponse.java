package com.commerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
// Standard structure
public class APIResponse {
    public String message;
    private boolean status;
}
