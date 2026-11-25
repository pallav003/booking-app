package com.example.demo.service.discovery;

import com.example.demo.domain.model.Maid;
import org.springframework.stereotype.Component;

@Component
public class LocalityFilter implements MaidFilter {
    @Override
    public boolean matches(Maid maid, MaidSearchCriteria criteria) {
        if (criteria.locality() == null || criteria.locality().isBlank()) {
            return true;
        }
        return maid.getLocality() != null
                && maid.getLocality().equalsIgnoreCase(criteria.locality().trim());
    }
}
