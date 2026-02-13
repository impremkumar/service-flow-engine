package com.ebtedge.service.flow.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfileDataTest {

    @Test
    void testProfileDataCreation() {
        Balance balance = new Balance("CLIENT-123", 1500.00);
        Demographics demographics = new Demographics("John Doe", "john@example.com", "Boston");

        ProfileData profileData = new ProfileData(balance, demographics);

        assertEquals(balance, profileData.balance());
        assertEquals(demographics, profileData.demographics());
    }

    @Test
    void testProfileDataEquality() {
        Balance balance = new Balance("CLIENT-123", 1500.00);
        Demographics demographics = new Demographics("John Doe", "john@example.com", "Boston");

        ProfileData data1 = new ProfileData(balance, demographics);
        ProfileData data2 = new ProfileData(balance, demographics);

        assertEquals(data1, data2);
        assertEquals(data1.hashCode(), data2.hashCode());
    }

    @Test
    void testProfileDataToString() {
        Balance balance = new Balance("CLIENT-123", 1500.00);
        Demographics demographics = new Demographics("John Doe", "john@example.com", "Boston");

        ProfileData profileData = new ProfileData(balance, demographics);

        String str = profileData.toString();
        assertTrue(str.contains("CLIENT-123"));
        assertTrue(str.contains("John Doe"));
    }
}
