package com.donorconnect.ai;

import com.donorconnect.security.UserPrincipal;

public interface AiService {
    AiRequestSummaryResponse generateSummary(UserPrincipal principal, Long requestId);
    DuplicateCheckResponse checkForDuplicates(UserPrincipal principal, Long requestId);
}
