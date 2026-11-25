package com.example.demo.service.discovery;

import com.example.demo.domain.model.Maid;
import org.springframework.stereotype.Component;

@Component
public class PriceFilter implements MaidFilter {
    @Override
    public boolean matches(Maid maid, MaidSearchCriteria criteria) {
        if (criteria.maxHourlyRate() == null) {
            return true;
        }
        return maid.getHourlyRate() != null
                && maid.getHourlyRate().compareTo(criteria.maxHourlyRate()) <= 0;
    }
}
