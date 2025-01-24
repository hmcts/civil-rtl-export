package uk.gov.hmcts.reform.civil.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.civil.domain.Judgment;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JudgmentRepository extends JpaRepository<Judgment, Long> {
    @Query("SELECT j FROM Judgment j WHERE ((:rerun = true AND j.reportedToRtl = :asOf) "
           + "OR (:rerun = false AND j.reportedToRtl IS NULL)) AND j.serviceId = :serviceId")
    List<Judgment> findForUpdate(@Param("rerun") boolean rerun,
                                 @Param("asOf") LocalDateTime asOf, @Param("serviceId") String serviceId);

    @Query("SELECT DISTINCT j.serviceId FROM Judgment j WHERE j.reportedToRtl IS NULL")
    List<String> findActiveServiceIds();

    @Modifying
    @Query("DELETE FROM Judgment j WHERE j.reportedToRtl < :dateOfDeletion")
    int deleteJudgmentsBefore(@Param("dateOfDeletion") LocalDateTime dateOfDeletion);

    List<Judgment> findByServiceIdAndReportedToRtl(String serviceId, LocalDateTime asOf);

    List<Judgment> findByServiceId(String serviceId);
}
