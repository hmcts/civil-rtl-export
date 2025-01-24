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
//interface extends JpaRepository enabling ability to write and execute JDBC queries
public interface JudgmentRepository extends JpaRepository<Judgment, Long> {
    //defining methods to query from the judgements table

    /* query to retrieve judgments that meet the rerun and asOf conditions for the serviceId
    "SELECT FOR UPDATE columns FROM judgments WHERE ((rerun AND reported_to_rtl = asOf) OR reported_to_rtl IS NULL)
    AND service_id = serviceId"

    Selects judgments that either haven't been reported yet (reported_to_rtl is NULL) or need a rerun
    as reported_to_rtl is before/equal to the asOf time */
    @Query("SELECT j FROM Judgment j WHERE ((:rerun = true AND j.reportedToRtl = :asOf) "
           + "OR (:rerun = false AND j.reportedToRtl IS NULL)) AND j.serviceId = :serviceId")
    List<Judgment> findForUpdate(@Param("rerun") boolean rerun,
                                 @Param("asOf") LocalDateTime asOf, @Param("serviceId") String serviceId);

    //query to find all service IDs that have unreported judgments (reported_to_rtl is NULL)
    //"If no serviceId was given, for each active serviceId (otherwise run this just for the provided serviceId)"
    //fetches serviceIds where some judgments have not been reported to RTL yet
    @Query("SELECT DISTINCT j.serviceId FROM Judgment j WHERE j.reportedToRtl IS NULL")
    List<String> findActiveServiceIds();
    /*
    A JPA query will be used to delete the judgment events.  The SQL equivalent of the query will be:
    DELETE
    FROM   judgments
    WHERE  reported_to_rtl + Interval '<number_of_days> day' > CURRENT_TIMESTAMP
    The housekeeping process will be set up as a Spring scheduled task.
    */

    @Modifying
    @Query("DELETE FROM Judgment j WHERE j.reportedToRtl < :dateOfDeletion")
    int deleteJudgmentsBefore(@Param("dateOfDeletion") LocalDateTime dateOfDeletion);

    //Retrieves judgments by service ID and reportedToRtl timestamp
    List<Judgment> findByServiceIdAndReportedToRtl(String serviceId, LocalDateTime asOf);

    //Retrieving judgments by only service ID
    List<Judgment> findByServiceId(String serviceId);
}
