package uk.gov.hmcts.reform.civil.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.civil.domain.Judgment;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JudgmentRepository extends JpaRepository<Judgment, Long> {

    String PARAM_RERUN = "rerun";
    String PARAM_AS_OF = "asOf";
    String PARAM_SERVICE_ID = "serviceId";

    String PARAM_DATE_OF_DELETION = "dateOfDeletion";

    @Query("SELECT j FROM Judgment j WHERE ((:rerun = true AND j.reportedToRtl = :asOf) "
        + "OR (:rerun = false AND j.reportedToRtl IS NULL))")
    List<Judgment> findForUpdate(@Param(PARAM_RERUN) boolean rerun, @Param(PARAM_AS_OF) LocalDateTime asOf);

    @Query("SELECT j FROM Judgment j WHERE ((:rerun = true AND j.reportedToRtl = :asOf) "
        + "OR (:rerun = false AND j.reportedToRtl IS NULL)) AND j.serviceId = :serviceId")
    List<Judgment> findForUpdateByServiceId(@Param(PARAM_RERUN) boolean rerun,
                                            @Param(PARAM_AS_OF) LocalDateTime asOf,
                                            @Param(PARAM_SERVICE_ID) String serviceId);

    @Modifying
    @Query("DELETE FROM Judgment j WHERE j.reportedToRtl < :dateOfDeletion")
    int deleteJudgmentsBefore(@Param(PARAM_DATE_OF_DELETION) LocalDateTime dateOfDeletion);
}
