package com.brainchain.career_counselling.Career_Counselling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResult {
    private Long userId;
    private String userEmail;
    private IQTestResult iqTestResult;
    private MITestResult miTestResult;
    private RIASECResult riasecResult;
    private MBTITestResult mbtiTestResult;
}
