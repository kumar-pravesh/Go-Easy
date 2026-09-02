package com.ride.goeasy.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ride.goeasy.entity.Booking;
import com.ride.goeasy.repository.BookingRepo;

/**
 * Safety Monitor Task — runs every 10 minutes.
 *
 * Logic (time-based stagnation — no external API needed):
 *   1. Parse estimatedTime from the Booking (e.g. "25 mins" → 25 minutes).
 *   2. Add a 50% grace buffer (so 25 mins → alert threshold = 37.5 mins).
 *   3. If a ride has been ONGOING longer than that threshold, send a
 *      safety-check email to the customer asking "Are you okay?"
 *
 * This avoids expensive OpenRouteService polling and is production-safe
 * at any ride volume.
 */
@Component
public class SafetyMonitorTask {

    private final BookingRepo bookingRepo;
    private final MailService mailService;

    public SafetyMonitorTask(BookingRepo bookingRepo, MailService mailService) {
        this.bookingRepo = bookingRepo;
        this.mailService = mailService;
    }

    // Runs every 10 minutes
    @Scheduled(fixedRate = 600_000)
    public void checkForDelayedRides() {
        // Find rides started more than 10 minutes ago (minimum gate to avoid false alarms)
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);
        List<Booking> ongoingRides = bookingRepo.findOngoingRidesStartedBefore(cutoff);

        for (Booking b : ongoingRides) {
            try {
                int estimatedMins = parseEstimatedMinutes(b.getEstimatedTime());
                if (estimatedMins <= 0) continue;

                // Alert threshold = estimated time + 50% grace buffer
                long thresholdMins = Math.round(estimatedMins * 1.5);
                LocalDateTime alertTime = b.getRideStartedAt().plusMinutes(thresholdMins);

                if (LocalDateTime.now().isAfter(alertTime)) {
                    sendSafetyCheckMail(b, estimatedMins);
                }
            } catch (Exception e) {
                System.err.println("[SafetyMonitor] Error checking booking " + b.getId() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Parses estimatedTime strings like "25 mins", "1 hr 10 mins", "40 min"
     * into total minutes. Returns -1 if unparseable.
     */
    private int parseEstimatedMinutes(String estimatedTime) {
        if (estimatedTime == null || estimatedTime.isBlank()) return -1;

        String lower = estimatedTime.toLowerCase();
        int totalMins = 0;

        // Hours
        java.util.regex.Matcher hrMatcher = java.util.regex.Pattern.compile("(\\d+)\\s*hr").matcher(lower);
        if (hrMatcher.find()) {
            totalMins += Integer.parseInt(hrMatcher.group(1)) * 60;
        }

        // Minutes
        java.util.regex.Matcher minMatcher = java.util.regex.Pattern.compile("(\\d+)\\s*min").matcher(lower);
        if (minMatcher.find()) {
            totalMins += Integer.parseInt(minMatcher.group(1));
        }

        return totalMins > 0 ? totalMins : -1;
    }

    private void sendSafetyCheckMail(Booking b, int estimatedMins) {
        if (b.getCustomer() == null || b.getCustomer().getEmail() == null) return;

        String trackLink = "http://localhost:5173/public-track/" + b.getId();
        String subject = "⚠️ Go-Easy Safety Check — Is your ride okay?";
        String body = "Hello " + b.getCustomer().getName() + ",\n\n"
                + "We noticed your ride from " + b.getSourceLocation()
                + " to " + b.getDestinationLocation() + " has been ongoing for longer than expected.\n\n"
                + "Estimated time: " + estimatedMins + " minutes.\n\n"
                + "If everything is fine, no action is needed.\n"
                + "If you feel unsafe, please trigger the SOS button in the Go-Easy app immediately "
                + "or share your live location with someone you trust:\n\n"
                + trackLink + "\n\n"
                + "Your safety is our priority.\n\n"
                + "– Go-Easy Safety Team";

        try {
            mailService.sendMail(b.getCustomer().getEmail(), subject, body);
            System.out.println("[SafetyMonitor] Safety check mail sent for booking #" + b.getId());
        } catch (Exception e) {
            System.err.println("[SafetyMonitor] Failed to send safety mail for booking #" + b.getId() + ": " + e.getMessage());
        }
    }
}
