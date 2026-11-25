package com.example.demo.service.discovery;

import com.example.demo.domain.model.Maid;
import org.springframework.stereotype.Component;

@Component
public class GenderFilter implements MaidFilter {
    @Override
    public boolean matches(Maid maid, MaidSearchCriteria criteria) {
        if (criteria.gender() == null) {
            return true;
        }
        return maid.getGender() == criteria.gender();
    }
}
