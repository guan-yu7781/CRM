package com.crm.personal.crm.deal;

/**
 * Classifies a deal as a new-customer acquisition or an expansion/upsell
 * opportunity for an existing active customer.
 */
public enum OpportunityType {
    /** Existing ACTIVE customer — cross-sell, upsell, renewal */
    EXPANSION,
    /** LEAD or INACTIVE customer — first-time or re-acquisition */
    ACQUISITION
}
