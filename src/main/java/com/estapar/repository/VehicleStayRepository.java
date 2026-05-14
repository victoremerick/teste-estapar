package com.estapar.repository;

import com.estapar.entity.VehicleStay;
import com.estapar.enums.StayStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleStayRepository extends JpaRepository<VehicleStay, Long> {

    Optional<VehicleStay> findFirstByLicensePlateAndExitTimeIsNullOrderByEntryTimeDesc(String licensePlate);

    @Query("""
            select coalesce(sum(vs.totalAmount), 0)
            from VehicleStay vs
            where vs.sector.name = :sector
              and vs.status = :status
              and vs.exitTime >= :start
              and vs.exitTime < :end
            """)
    BigDecimal sumTotalAmountBySectorAndExitTimeBetween(@Param("sector") String sector,
                                                        @Param("status") StayStatus status,
                                                        @Param("start") Instant start,
                                                        @Param("end") Instant end);
}
