package com.example.demo.service.discovery;

import com.example.demo.domain.model.Maid;

/**
 * Pluggable filter contract for maid discovery.
 * New filters can be added as Spring components without modifying MaidService.
 */
public interface MaidFilter {
    boolean matches(Maid maid, MaidSearchCriteria criteria);
}
