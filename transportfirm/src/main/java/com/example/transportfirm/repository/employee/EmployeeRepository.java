package com.example.transportfirm.repository.employee;

import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.enums.EmploymentStatus;
import com.example.transportfirm.enums.JobTitle;
import com.example.transportfirm.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByEgn(String egn);
    List<Employee> findByJobTitle(JobTitle jobTitle);
    Optional<Employee> findByEmail(String email);
    List<Employee> findByEmploymentStatus(EmploymentStatus status);
    boolean existsByRole(Role role);

    /**
     * Loads all employees with their role-specific info entities in a single query,
     * eliminating N+1 lazy-load on driverInfo / mechanicInfo / dispatcherInfo.
     * Documents are handled separately via @BatchSize on the entity.
     */
    @Query("SELECT DISTINCT e FROM Employee e " +
           "LEFT JOIN FETCH e.driverInfo di " +
           "LEFT JOIN FETCH di.vehicle " +
           "LEFT JOIN FETCH e.mechanicInfo " +
           "LEFT JOIN FETCH e.dispatcherInfo")
    List<Employee> findAllWithInfos();

    /** Single employee with all associations loaded (for detail view). */
    @Query("SELECT e FROM Employee e " +
           "LEFT JOIN FETCH e.driverInfo di " +
           "LEFT JOIN FETCH di.vehicle " +
           "LEFT JOIN FETCH e.mechanicInfo " +
           "LEFT JOIN FETCH e.dispatcherInfo " +
           "WHERE e.id = :id")
    Optional<Employee> findByIdWithInfos(@Param("id") UUID id);

    /** All employees of a given job title with infos — eliminates N+1 on role-specific info. */
    @Query("SELECT DISTINCT e FROM Employee e " +
           "LEFT JOIN FETCH e.driverInfo di " +
           "LEFT JOIN FETCH di.vehicle " +
           "LEFT JOIN FETCH e.mechanicInfo " +
           "LEFT JOIN FETCH e.dispatcherInfo " +
           "WHERE e.jobTitle = :jobTitle")
    List<Employee> findByJobTitleWithInfos(@Param("jobTitle") JobTitle jobTitle);

    /** Lookup by email with infos loaded — used by controllers that access mechanicInfo/dispatcherInfo. */
    @Query("SELECT e FROM Employee e " +
           "LEFT JOIN FETCH e.driverInfo di " +
           "LEFT JOIN FETCH di.vehicle " +
           "LEFT JOIN FETCH e.mechanicInfo " +
           "LEFT JOIN FETCH e.dispatcherInfo " +
           "WHERE e.email = :email")
    Optional<Employee> findByEmailWithInfos(@Param("email") String email);

    /** Returns distinct emails of employees with the given roles, excluding null/blank emails. */
    @Query("SELECT DISTINCT e.email FROM Employee e WHERE e.role IN :roles AND e.email IS NOT NULL AND e.email <> ''")
    List<String> findEmailsByRoles(@Param("roles") List<Role> roles);

    /** Returns distinct non-blank phone numbers of active employees with the given roles. */
    @Query("SELECT DISTINCT e.phone FROM Employee e WHERE e.role IN :roles AND e.phone IS NOT NULL AND e.phone <> '' AND e.employmentStatus = com.example.transportfirm.enums.EmploymentStatus.ACTIVE")
    List<String> findPhonesByRoles(@Param("roles") List<Role> roles);

    // Paginated driver list — supports text search + document-status filter
    @Query(value =
           "SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.driverInfo d WHERE e.jobTitle = :jobTitle " +
           "AND (:search = '' OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) OR e.egn LIKE CONCAT('%', :search, '%')) " +
           "AND (:status = '' " +
           "  OR (:status = 'missing' AND (d IS NULL OR (d.driverLicenseExpiresOn IS NULL AND d.qualificationCardExpiresOn IS NULL AND d.psychologicalExamExpiresOn IS NULL AND d.digitalCardExpiresOn IS NULL))) " +
           "  OR (:status = 'expired' AND (" +
           "       (d.driverLicenseExpiresOn IS NOT NULL AND d.driverLicenseExpiresOn < CURRENT_DATE) OR " +
           "       (d.qualificationCardExpiresOn IS NOT NULL AND d.qualificationCardExpiresOn < CURRENT_DATE) OR " +
           "       (d.psychologicalExamExpiresOn IS NOT NULL AND d.psychologicalExamExpiresOn < CURRENT_DATE) OR " +
           "       (d.digitalCardExpiresOn IS NOT NULL AND d.digitalCardExpiresOn < CURRENT_DATE))) " +
           "  OR (:status = 'expiring' AND NOT (" +
           "       (d.driverLicenseExpiresOn IS NOT NULL AND d.driverLicenseExpiresOn < CURRENT_DATE) OR " +
           "       (d.qualificationCardExpiresOn IS NOT NULL AND d.qualificationCardExpiresOn < CURRENT_DATE) OR " +
           "       (d.psychologicalExamExpiresOn IS NOT NULL AND d.psychologicalExamExpiresOn < CURRENT_DATE) OR " +
           "       (d.digitalCardExpiresOn IS NOT NULL AND d.digitalCardExpiresOn < CURRENT_DATE)) AND (" +
           "       (d.driverLicenseExpiresOn IS NOT NULL AND d.driverLicenseExpiresOn BETWEEN CURRENT_DATE AND :expiryDate) OR " +
           "       (d.qualificationCardExpiresOn IS NOT NULL AND d.qualificationCardExpiresOn BETWEEN CURRENT_DATE AND :expiryDate) OR " +
           "       (d.psychologicalExamExpiresOn IS NOT NULL AND d.psychologicalExamExpiresOn BETWEEN CURRENT_DATE AND :expiryDate) OR " +
           "       (d.digitalCardExpiresOn IS NOT NULL AND d.digitalCardExpiresOn BETWEEN CURRENT_DATE AND :expiryDate))) " +
           "  OR (:status = 'valid' AND d IS NOT NULL AND " +
           "       (d.driverLicenseExpiresOn IS NOT NULL OR d.qualificationCardExpiresOn IS NOT NULL OR d.psychologicalExamExpiresOn IS NOT NULL OR d.digitalCardExpiresOn IS NOT NULL) AND NOT (" +
           "       (d.driverLicenseExpiresOn IS NOT NULL AND d.driverLicenseExpiresOn < :expiryDate) OR " +
           "       (d.qualificationCardExpiresOn IS NOT NULL AND d.qualificationCardExpiresOn < :expiryDate) OR " +
           "       (d.psychologicalExamExpiresOn IS NOT NULL AND d.psychologicalExamExpiresOn < :expiryDate) OR " +
           "       (d.digitalCardExpiresOn IS NOT NULL AND d.digitalCardExpiresOn < :expiryDate))))",
           countQuery =
           "SELECT COUNT(DISTINCT e) FROM Employee e LEFT JOIN e.driverInfo d WHERE e.jobTitle = :jobTitle " +
           "AND (:search = '' OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) OR e.egn LIKE CONCAT('%', :search, '%')) " +
           "AND (:status = '' " +
           "  OR (:status = 'missing' AND (d IS NULL OR (d.driverLicenseExpiresOn IS NULL AND d.qualificationCardExpiresOn IS NULL AND d.psychologicalExamExpiresOn IS NULL AND d.digitalCardExpiresOn IS NULL))) " +
           "  OR (:status = 'expired' AND (" +
           "       (d.driverLicenseExpiresOn IS NOT NULL AND d.driverLicenseExpiresOn < CURRENT_DATE) OR " +
           "       (d.qualificationCardExpiresOn IS NOT NULL AND d.qualificationCardExpiresOn < CURRENT_DATE) OR " +
           "       (d.psychologicalExamExpiresOn IS NOT NULL AND d.psychologicalExamExpiresOn < CURRENT_DATE) OR " +
           "       (d.digitalCardExpiresOn IS NOT NULL AND d.digitalCardExpiresOn < CURRENT_DATE))) " +
           "  OR (:status = 'expiring' AND NOT (" +
           "       (d.driverLicenseExpiresOn IS NOT NULL AND d.driverLicenseExpiresOn < CURRENT_DATE) OR " +
           "       (d.qualificationCardExpiresOn IS NOT NULL AND d.qualificationCardExpiresOn < CURRENT_DATE) OR " +
           "       (d.psychologicalExamExpiresOn IS NOT NULL AND d.psychologicalExamExpiresOn < CURRENT_DATE) OR " +
           "       (d.digitalCardExpiresOn IS NOT NULL AND d.digitalCardExpiresOn < CURRENT_DATE)) AND (" +
           "       (d.driverLicenseExpiresOn IS NOT NULL AND d.driverLicenseExpiresOn BETWEEN CURRENT_DATE AND :expiryDate) OR " +
           "       (d.qualificationCardExpiresOn IS NOT NULL AND d.qualificationCardExpiresOn BETWEEN CURRENT_DATE AND :expiryDate) OR " +
           "       (d.psychologicalExamExpiresOn IS NOT NULL AND d.psychologicalExamExpiresOn BETWEEN CURRENT_DATE AND :expiryDate) OR " +
           "       (d.digitalCardExpiresOn IS NOT NULL AND d.digitalCardExpiresOn BETWEEN CURRENT_DATE AND :expiryDate))) " +
           "  OR (:status = 'valid' AND d IS NOT NULL AND " +
           "       (d.driverLicenseExpiresOn IS NOT NULL OR d.qualificationCardExpiresOn IS NOT NULL OR d.psychologicalExamExpiresOn IS NOT NULL OR d.digitalCardExpiresOn IS NOT NULL) AND NOT (" +
           "       (d.driverLicenseExpiresOn IS NOT NULL AND d.driverLicenseExpiresOn < :expiryDate) OR " +
           "       (d.qualificationCardExpiresOn IS NOT NULL AND d.qualificationCardExpiresOn < :expiryDate) OR " +
           "       (d.psychologicalExamExpiresOn IS NOT NULL AND d.psychologicalExamExpiresOn < :expiryDate) OR " +
           "       (d.digitalCardExpiresOn IS NOT NULL AND d.digitalCardExpiresOn < :expiryDate))))")
    Page<Employee> searchDriversWithStatus(
            @Param("jobTitle") JobTitle jobTitle,
            @Param("search") String search,
            @Param("status") String status,
            @Param("expiryDate") LocalDate expiryDate,
            Pageable pageable);

    // Paginated search by job title — mechanics, dispatchers
    @Query(value = "SELECT DISTINCT e FROM Employee e " +
                   "LEFT JOIN FETCH e.driverInfo LEFT JOIN FETCH e.mechanicInfo LEFT JOIN FETCH e.dispatcherInfo " +
                   "WHERE e.jobTitle = :jobTitle " +
                   "AND (:search = '' OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) OR e.egn LIKE CONCAT('%', :search, '%'))",
           countQuery = "SELECT COUNT(e) FROM Employee e WHERE e.jobTitle = :jobTitle " +
                        "AND (:search = '' OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) OR e.egn LIKE CONCAT('%', :search, '%'))")
    Page<Employee> searchByJobTitle(
            @Param("jobTitle") JobTitle jobTitle,
            @Param("search") String search,
            Pageable pageable);

    // Paginated search across all employees
    @Query(value = "SELECT DISTINCT e FROM Employee e " +
                   "LEFT JOIN FETCH e.driverInfo LEFT JOIN FETCH e.mechanicInfo LEFT JOIN FETCH e.dispatcherInfo " +
                   "WHERE (:search = '' OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) OR e.egn LIKE CONCAT('%', :search, '%'))",
           countQuery = "SELECT COUNT(e) FROM Employee e WHERE " +
                        "(:search = '' OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) OR e.egn LIKE CONCAT('%', :search, '%'))")
    Page<Employee> searchAll(@Param("search") String search, Pageable pageable);

    // ── Dashboard aggregate queries (avoid findAll for counting/summing) ──────

    long countByEmploymentStatus(EmploymentStatus status);

    @Query("SELECT COALESCE(SUM(e.salary), 0) FROM Employee e WHERE e.employmentStatus = :status")
    BigDecimal sumSalaryByEmploymentStatus(@Param("status") EmploymentStatus status);

    @Query("SELECT COALESCE(SUM(e.salaryNeto), 0) FROM Employee e WHERE e.employmentStatus = :status")
    BigDecimal sumSalaryNetoByEmploymentStatus(@Param("status") EmploymentStatus status);
}
