package com.hospital.model;

/**
 * Interface demonstrating OOP Abstraction and Polymorphism.
 * Any service, consultation, room stay, or procedure that can be billed
 * must implement this contract.
 */
public interface Billable {
    /**
     * Calculates and returns the billable amount before taxes.
     */
    double calculateSubtotal();

    /**
     * Returns a human-readable description of the billed service.
     */
    String getBillingDescription();
}
