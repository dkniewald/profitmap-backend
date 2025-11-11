package com.profitmap_backend.model;

public enum DocumentRelationshipType {
    // Offer to Invoice relationships
    OFFER_TO_INVOICE,           // Standard offer conversion to invoice
    PARTIAL_OFFER_TO_INVOICE,   // Partial offer conversion
    
    // Invoice to Offer relationships  
    INVOICE_TO_OFFER,           // Invoice converted back to offer (rare case)
    
    // Complex relationships
    MULTIPLE_OFFERS_TO_INVOICE, // Multiple offers combined into one invoice
    OFFER_REFERENCE,            // Invoice references an offer for context
    INVOICE_REFERENCE,          // Offer references an invoice for context
    
    // Business relationships
    REPLACEMENT,                // One document replaces another
    AMENDMENT,                  // One document amends another
    CANCELLATION                // One document cancels another
}
