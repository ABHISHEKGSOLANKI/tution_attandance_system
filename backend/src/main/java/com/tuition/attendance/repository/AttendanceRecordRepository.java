package com.tuition.attendance.repository;

import com.tuition.attendance.entities.AttendanceRecord;
import com.tuition.attendance.model.StudentClass;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findByStudentIdAndAttendanceDate(Long studentId, LocalDate attendanceDate);

    @Query("""
            select ar from AttendanceRecord ar
            where (:studentId is null or ar.student.id = :studentId)
              and (:studentClass is null or ar.student.studentClass = :studentClass)
              and (:date is null or ar.attendanceDate = :date)
            order by ar.attendanceDate desc, ar.student.name asc
            """)
    List<AttendanceRecord> search(@Param("studentId") Long studentId,
                                  @Param("studentClass") StudentClass studentClass,
                                  @Param("date") LocalDate date);

    @Query("""
            select ar from AttendanceRecord ar
            where ar.student.id = :studentId
            order by ar.attendanceDate desc
            """)
    List<AttendanceRecord> findStudentHistory(@Param("studentId") Long studentId);

    @Query("""
            select count(ar) from AttendanceRecord ar
            where ar.student.id = :studentId and ar.attendanceDate between :start and :end
            """)
    long countPresentBetween(@Param("studentId") Long studentId,
                             @Param("start") LocalDate start,
                             @Param("end") LocalDate end);

    @Query("""
            select ar.attendanceDate, count(ar)
            from AttendanceRecord ar
            where ar.attendanceDate between :start and :end
            group by ar.attendanceDate
            order by ar.attendanceDate
            """)
    List<Object[]> countDailyBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("""
            select count(ar) from AttendanceRecord ar
            where ar.attendanceDate between :start and :end
            """)
    long countBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
