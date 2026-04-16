package com.example.eduskill.repository;

import com.example.eduskill.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmployeeCode(String employeeCode);

    Optional<User> findByEmail(String username);

    @Query("""
    SELECT u FROM User u
    WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))
""")
    List<User> searchByName(@Param("name") String name);

    @Query("""
    SELECT u
    FROM User u
    WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))
""")
    List<User> searchSalesmanByName(@Param("name") String name);



    @Query("""
    SELECT u
    FROM User u
    WHERE u.employeeCode IS NOT NULL
""")
    List<User> findAllSalesmans();





    Page<User> findByEnableTrue(Pageable pageable);

    Page<User> findByEnableFalse(Pageable pageable);


}
