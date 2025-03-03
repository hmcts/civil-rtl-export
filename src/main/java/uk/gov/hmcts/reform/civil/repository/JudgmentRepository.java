package uk.gov.hmcts.reform.civil.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.civil.domain.Judgment;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JudgmentRepository extends JpaRepository<Judgment, Long> {

    default List<Judgment> findByEventDetails(String serviceId,
                                              String judgmentId,
                                              LocalDateTime judgmentEventTimestamp,
                                              String caseNumber) {
        Sort sortByJudgmentIdAsc = Sort.by(Sort.Direction.ASC, "judgmentId");
        return findByEventDetails(serviceId, judgmentId, judgmentEventTimestamp, caseNumber, sortByJudgmentIdAsc);
    }

    @Query("SELECT j FROM Judgment j "
        + "WHERE j.serviceId = :serviceId "
        + "AND j.judgmentEventTimestamp = :timestamp "
        + "AND j.caseNumber = :caseNumber "
        + "AND (j.judgmentId = :#{#judgmentId + '-1'} OR j.judgmentId = :#{#judgmentId + '-2'})")
    List<Judgment> findByEventDetails(@Param("serviceId") String serviceId,
                                      @Param("judgmentId") String judgmentId,
                                      @Param("timestamp") LocalDateTime judgmentEventTimestamp,
                                      @Param("caseNumber") String caseNumber,
                                      Sort sort);
}
