package com.ride.goeasy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ride.goeasy.entity.Booking;
import com.ride.goeasy.entity.Company;
import com.ride.goeasy.entity.CorporateWallet;
import com.ride.goeasy.entity.Customer;
import com.ride.goeasy.entity.Driver;
import com.ride.goeasy.entity.Payment;
import com.ride.goeasy.entity.PromoCode;
import com.ride.goeasy.entity.SOSEvent;
import com.ride.goeasy.entity.Userr;
import com.ride.goeasy.entity.Vehicle;
import com.ride.goeasy.enums.BookingStatus;
import com.ride.goeasy.repository.BookingRepo;
import com.ride.goeasy.repository.CompanyRepo;
import com.ride.goeasy.repository.CorporateWalletRepo;
import com.ride.goeasy.repository.CustomerRepo;
import com.ride.goeasy.repository.DriverRepo;
import com.ride.goeasy.repository.PaymentRepo;
import com.ride.goeasy.repository.PromoCodeRepo;
import com.ride.goeasy.repository.SOSEventRepository;
import com.ride.goeasy.repository.TrustedContactRepository;
import com.ride.goeasy.repository.UserrRepo;
import com.ride.goeasy.repository.VehicleRepo;

@Component
public class DataSeeder implements CommandLineRunner {

    private final SOSEventRepository sosEventRepo;
    private final PaymentRepo paymentRepo;
    private final BookingRepo bookingRepo;
    private final CorporateWalletRepo corporateWalletRepo;
    private final PromoCodeRepo promoCodeRepo;
    private final CompanyRepo companyRepo;
    private final VehicleRepo vehicleRepo;
    private final DriverRepo driverRepo;
    private final CustomerRepo customerRepo;
    private final UserrRepo userrRepo;
    private final TrustedContactRepository trustedContactRepo;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final boolean seedOnStartup;

    public DataSeeder(SOSEventRepository sosEventRepo,
                      PaymentRepo paymentRepo,
                      BookingRepo bookingRepo,
                      CorporateWalletRepo corporateWalletRepo,
                      PromoCodeRepo promoCodeRepo,
                      CompanyRepo companyRepo,
                      VehicleRepo vehicleRepo,
                      DriverRepo driverRepo,
                      CustomerRepo customerRepo,
                      UserrRepo userrRepo,
                      TrustedContactRepository trustedContactRepo,
                      PasswordEncoder passwordEncoder,
                      JdbcTemplate jdbcTemplate,
                      @Value("${app.db.seed:false}") boolean seedOnStartup) {
        this.sosEventRepo = sosEventRepo;
        this.paymentRepo = paymentRepo;
        this.bookingRepo = bookingRepo;
        this.corporateWalletRepo = corporateWalletRepo;
        this.promoCodeRepo = promoCodeRepo;
        this.companyRepo = companyRepo;
        this.vehicleRepo = vehicleRepo;
        this.driverRepo = driverRepo;
        this.customerRepo = customerRepo;
        this.userrRepo = userrRepo;
        this.trustedContactRepo = trustedContactRepo;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.seedOnStartup = seedOnStartup;
    }

    @Override
    public void run(String... args) {
        System.out.println("DataSeeder starting: clearing old data and reseeding dummy data.");
        cleanDatabase();
        seedDummyData();
    }

    public void resetDatabase() {
        cleanDatabase();
        seedDummyData();
    }

    private void cleanDatabase() {
        try {
            jdbcTemplate.execute("DROP TABLE IF EXISTS driver_dblist");
            System.out.println("Dropped stale join table driver_dblist.");
        } catch (Exception e) {
            System.out.println("Stale join table cleanup skipped: " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE booking DROP COLUMN IF EXISTS payment_id");
            System.out.println("Dropped stale booking.payment_id column.");
        } catch (Exception e) {
            System.out.println("Stale payment column cleanup skipped: " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE payment ADD COLUMN IF NOT EXISTS customer_confirmed_cash BOOLEAN DEFAULT FALSE;");
            jdbcTemplate.execute("ALTER TABLE payment ADD COLUMN IF NOT EXISTS driver_confirmed_cash BOOLEAN DEFAULT FALSE;");
            jdbcTemplate.execute("ALTER TABLE payment ADD COLUMN IF NOT EXISTS payment_disputed_at TIMESTAMP;");
            jdbcTemplate.execute("ALTER TABLE payment ADD COLUMN IF NOT EXISTS last_confirmed_at TIMESTAMP;");
            System.out.println("Ensured payment table has the correct columns.");
        } catch (Exception e) {
            System.out.println("Payment table schema checked/updated manually: " + e.getMessage());
        }

        sosEventRepo.deleteAllInBatch();
        paymentRepo.deleteAllInBatch();
        bookingRepo.deleteAllInBatch();
        corporateWalletRepo.deleteAllInBatch();
        promoCodeRepo.deleteAllInBatch();
        companyRepo.deleteAllInBatch();
        vehicleRepo.deleteAllInBatch();
        driverRepo.deleteAllInBatch();
        trustedContactRepo.deleteAllInBatch();
        customerRepo.deleteAllInBatch();
        userrRepo.deleteAllInBatch();
        System.out.println("Database cleanup complete.");
    }

    private void seedDummyData() {
        // Customers
        Userr customerUser1 = new Userr(9000000001L, passwordEncoder.encode("password123"), "CUSTOMER");
        Userr customerUser2 = new Userr(9000000002L, passwordEncoder.encode("password123"), "CUSTOMER");
        userrRepo.save(customerUser1);
        userrRepo.save(customerUser2);

        Customer customer1 = new Customer();
        customer1.setName("Aditi Singh");
        customer1.setAge(27);
        customer1.setGender("Female");
        customer1.setPassword("pass123");
        customer1.setMobno(9000000001L);
        customer1.setEmail("aditi@example.com");
        customer1.setCurrentLocation("Hyderabad,17.3850,78.4867");
        customer1.setUserr(customerUser1);
        customer1.setActiveBookingFlag(false);
        customer1.setCancellationCount(0);

        Customer customer2 = new Customer();
        customer2.setName("Arjun Patel");
        customer2.setAge(31);
        customer2.setGender("Male");
        customer2.setPassword("pass123");
        customer2.setMobno(9000000002L);
        customer2.setEmail("arjun@example.com");
        customer2.setCurrentLocation("Bangalore,12.9716,77.5946");
        customer2.setUserr(customerUser2);
        customer2.setActiveBookingFlag(false);
        customer2.setCancellationCount(1);

        customerRepo.save(customer1);
        customerRepo.save(customer2);

        // Drivers and Vehicles
        Userr driverUser1 = new Userr(9100000001L, passwordEncoder.encode("driverpass"), "DRIVER");
        Userr driverUser2 = new Userr(9100000002L, passwordEncoder.encode("driverpass"), "DRIVER");
        userrRepo.save(driverUser1);
        userrRepo.save(driverUser2);

        Driver driver1 = new Driver();
        driver1.setDname("Kavya Nair");
        driver1.setLicNo("TS09AB1234");
        driver1.setUpiId("kavya@upi");
        driver1.setDstatus("AVAILABLE");
        driver1.setAge(31);
        driver1.setMobNo(9100000001L);
        driver1.setGender("Female");
        driver1.setMailId("kavya.driver@example.com");
        driver1.setPassword("driverpass");
        driver1.setUserr(driverUser1);
        // Seed realistic driver rating data
        driver1.setDriverRating(4.8);
        driver1.setTotalRatings(12);

        Vehicle vehicle1 = new Vehicle();
        vehicle1.setVehicleName("Swift Dzire");
        vehicle1.setVehicleType("SEDAN");
        vehicle1.setVehicleNumber("TS09AB1234");
        vehicle1.setVehicleModel("2022");
        vehicle1.setVehicleCapacity(4);
        vehicle1.setCity("Hyderabad");
        vehicle1.setLatitude(17.3850);
        vehicle1.setLongitude(78.4867);
        vehicle1.setAvlStatus("AVAILABLE");
        vehicle1.setPricePerKm(11.0);
        vehicle1.setAvgspeed(42.0);
        driver1.setVehicle(vehicle1);

        Driver driver2 = new Driver();
        driver2.setDname("Rohan Mehta");
        driver2.setLicNo("KA05CD6789");
        driver2.setUpiId("rohan@upi");
        driver2.setDstatus("AVAILABLE");
        driver2.setAge(29);
        driver2.setMobNo(9100000002L);
        driver2.setGender("Male");
        driver2.setMailId("rohan.driver@example.com");
        driver2.setPassword("driverpass");
        driver2.setUserr(driverUser2);
        // Seed realistic driver rating data
        driver2.setDriverRating(4.8);
        driver2.setTotalRatings(12);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setVehicleName("Tigor");
        vehicle2.setVehicleType("HATCHBACK");
        vehicle2.setVehicleNumber("KA05CD6789");
        vehicle2.setVehicleModel("2023");
        vehicle2.setVehicleCapacity(4);
        vehicle2.setCity("Bangalore");
        vehicle2.setLatitude(12.9716);
        vehicle2.setLongitude(77.5946);
        vehicle2.setAvlStatus("AVAILABLE");
        vehicle2.setPricePerKm(10.0);
        vehicle2.setAvgspeed(38.0);
        driver2.setVehicle(vehicle2);

        driverRepo.save(driver1);
        driverRepo.save(driver2);

        // Bookings
        Booking booking1 = new Booking();
        booking1.setCustomer(customer1);
        booking1.setVehicle(vehicle1);
        booking1.setSourceLocation("Gachibowli");
        booking1.setDestinationLocation("HITEC City");
        booking1.setDistance(18.5);
        booking1.setFare(320.0);
        booking1.setEstimatedTime("35 mins");
        booking1.setBaseFare(50.0);
        booking1.setDistanceFare(180.0);
        booking1.setPenaltyAmount(0.0);
        booking1.setWaitingCharge(0.0);
        booking1.setNightCharge(0.0);
        booking1.setPlatformFee(20.0);
        booking1.setTax(15.0);
        booking1.setDiscount(0.0);
        booking1.setPricePerKm(11.0);
        booking1.setFareLocked(true);
        booking1.setStartOtp("1052");
        booking1.setEndOtp("3581");
        booking1.setStartOtpVerified(true);
        booking1.setEndOtpVerified(true);
        booking1.setDriver(driver1);
        driver1.getDblist().add(booking1);
        booking1.setBookingStatus(BookingStatus.COMPLETED);
        booking1.setActiveBookingFlag(false);
        booking1.setPaymentMode("UPI");
        booking1.setRideDate(LocalDate.now().minusDays(2));
        booking1.setScheduled(false);
        booking1.setFareLockedAtBooking(true);
        // Completed ride: customer rated driver 5 in seed data
        booking1.setDriverRating(5);

        Booking booking2 = new Booking();
        booking2.setCustomer(customer2);
        booking2.setVehicle(vehicle2);
        booking2.setSourceLocation("MG Road");
        booking2.setDestinationLocation("Whitefield");
        booking2.setDistance(22.0);
        booking2.setFare(390.0);
        booking2.setEstimatedTime("45 mins");
        booking2.setBaseFare(55.0);
        booking2.setDistanceFare(200.0);
        booking2.setPenaltyAmount(0.0);
        booking2.setWaitingCharge(0.0);
        booking2.setNightCharge(0.0);
        booking2.setPlatformFee(25.0);
        booking2.setTax(20.0);
        booking2.setDiscount(0.0);
        booking2.setPricePerKm(10.0);
        booking2.setFareLocked(true);
        booking2.setBookingStatus(BookingStatus.BOOKED);
        booking2.setDriver(driver2);
        driver2.getDblist().add(booking2);
        booking2.setActiveBookingFlag(true);
        booking2.setPaymentMode("CASH");
        booking2.setRideDate(LocalDate.now().plusDays(1));
        booking2.setScheduled(true);
        booking2.setScheduledTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        booking2.setScheduledNotifSent(false);
        booking2.setFareLockedAtBooking(false);

        Booking booking3 = new Booking();
        booking3.setCustomer(customer1);
        booking3.setVehicle(vehicle2);
        booking3.setSourceLocation("MG Road");
        booking3.setDestinationLocation("Koramangala");
        booking3.setDistance(8.0);
        booking3.setFare(140.0);
        booking3.setEstimatedTime("20 mins");
        booking3.setBaseFare(45.0);
        booking3.setDistanceFare(80.0);
        booking3.setPenaltyAmount(0.0);
        booking3.setWaitingCharge(0.0);
        booking3.setNightCharge(0.0);
        booking3.setPlatformFee(10.0);
        booking3.setTax(5.0);
        booking3.setDiscount(0.0);
        booking3.setDriver(driver2);
        driver2.getDblist().add(booking3);
        booking3.setPricePerKm(10.0);
        booking3.setFareLocked(false);
        booking3.setBookingStatus(BookingStatus.ONGOING);
        booking3.setActiveBookingFlag(true);
        booking3.setPaymentMode("UPI");
        booking3.setRideDate(LocalDate.now());
        booking3.setScheduled(false);
        booking3.setFareLockedAtBooking(false);

        bookingRepo.save(booking1);
        bookingRepo.save(booking2);
        bookingRepo.save(booking3);

        // Payment
        Payment payment1 = new Payment(customer1, vehicle1, booking1, 320.0, "UPI", "SUCCESS");
        paymentRepo.save(payment1);

        // SOS event for ongoing ride
        SOSEvent sosEvent = new SOSEvent(booking3, 12.9718, 77.6413);
        sosEventRepo.save(sosEvent);

        // Promo codes, corporate account and wallet
        PromoCode promoCode = new PromoCode();
        promoCode.setCode("FIRST50");
        promoCode.setDiscountPercent(50.0);
        promoCode.setMaxDiscountAmount(75.0);
        promoCode.setMinFare(150.0);
        promoCode.setValidUntil(LocalDate.now().plusMonths(3));
        promoCode.setDescription("50% off first ride up to ₹75");
        promoCodeRepo.save(promoCode);

        Company company = new Company();
        company.setCompanyName("Neon Tech Solutions");
        company.setCompanyEmail("corp@neontech.com");
        company.setPassword("corp123");
        company.setContactPerson("Rohan Patel");
        company.setGstNumber("29ABCDE1234F2Z5");
        company.setWalletBalance(7500.0);
        company.setMonthlyBudgetPerEmployee(1600.0);
        company.setActive(true);
        companyRepo.save(company);

        CorporateWallet corporateWallet = new CorporateWallet();
        corporateWallet.setCompany(company);
        corporateWallet.setCustomer(customer2);
        corporateWallet.setMonthlyBudget(1600.0);
        corporateWallet.setUsedThisMonth(320.0);
        corporateWallet.setBudgetMonth(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        corporateWallet.setActive(true);
        corporateWalletRepo.save(corporateWallet);

        System.out.println("Dummy data seeding complete.");
    }
}
