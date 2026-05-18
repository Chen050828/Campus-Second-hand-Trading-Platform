package com.campusmarket.repository;

import com.campusmarket.model.SystemAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemAccountRepository extends JpaRepository<SystemAccount, Long> {
}
