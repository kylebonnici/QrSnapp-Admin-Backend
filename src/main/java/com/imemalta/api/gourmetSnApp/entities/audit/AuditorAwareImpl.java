package com.imemalta.api.gourmetSnApp.entities.audit;


import com.imemalta.api.gourmetSnApp.utils.VariantUtils;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@SuppressWarnings("unused")
public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        String username = VariantUtils.currentSessionUsername();
        if (username != null) {
            return Optional.of(username);
        } else {
            return Optional.of("Anonymous");
        }
    }
}