package com.example.demo.service.discovery;

import com.example.demo.domain.model.Maid;
import org.springframework.stereotype.Component;

@Component
public class ServiceTypeFilter implements MaidFilter {
    @Override
    public boolean matches(Maid maid, MaidSearchCriteria criteria) {
        if (criteria.serviceType() == null) {
            return true;
        }
        return maid.hasSkill(criteria.serviceType());
    }
}
