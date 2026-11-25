package com.example.demo.service.discovery;

import com.example.demo.domain.model.Maid;
import org.springframework.stereotype.Component;

@Component
public class RatingFilter implements MaidFilter {
    @Override
    public boolean matches(Maid maid, MaidSearchCriteria criteria) {
        if (criteria.minRating() == null) {
            return true;
        }
        return maid.getRating() >= criteria.minRating();
    }
}
