package com.example.wordlearning.model;

import jakarta.validation.constraints.NotBlank;

public record RedeemCodeRequest(@NotBlank(message = "Activation code is required") String code) {
}
