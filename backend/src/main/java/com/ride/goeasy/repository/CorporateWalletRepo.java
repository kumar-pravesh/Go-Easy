package com.ride.goeasy.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ride.goeasy.entity.Company;
import com.ride.goeasy.entity.CorporateWallet;
import com.ride.goeasy.entity.Customer;

public interface CorporateWalletRepo extends JpaRepository<CorporateWallet, Integer> {
    Optional<CorporateWallet> findByCustomerAndActiveTrue(Customer customer);
    List<CorporateWallet> findByCompanyAndActiveTrue(Company company);
    boolean existsByCustomerAndActiveTrue(Customer customer);
}
