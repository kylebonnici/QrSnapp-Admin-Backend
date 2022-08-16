package com.imemalta.api.gourmetSnApp.permissionEvaluators;

import com.imemalta.api.gourmetSnApp.utils.VariantUtils;
import org.springframework.stereotype.Service;

@Service
public class IdentityPermissionsEvaluators {
    public boolean isLoggedInUser(String username) {
        return VariantUtils.currentSessionUsername().compareTo(username) == 0;
    }
}
