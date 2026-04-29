package com.tuition.attendance.service.admin;

import com.tuition.attendance.dto.ApprovalResponseDTO;
import com.tuition.attendance.dto.AttendanceRequestDTO;
import com.tuition.attendance.entities.AttendanceRecord;
import com.tuition.attendance.entities.AttendanceRequest;
import com.tuition.attendance.entities.User;
import com.tuition.attendance.exception.ApiException;
import com.tuition.attendance.model.AttendanceType;
import com.tuition.attendance.model.RequestStatus;
import com.tuition.attendance.repository.AttendanceRecordRepository;
import com.tuition.attendance.repository.AttendanceRequestRepository;
import com.tuition.attendance.repository.UserRepository;
import com.tuition.attendance.security.UserPrincipal;
import com.tuition.attendance.service.Mapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceRequestService {

    private final AttendanceRequestRepository attendanceRequestRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final UserRepository userRepository;

    public AttendanceRequestService(AttendanceRequestRepository attendanceRequestRepository,
                                    AttendanceRecordRepository attendanceRecordRepository,
                                    UserRepository userRepository) {
        this.attendanceRequestRepository = attendanceRequestRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AttendanceRequestDTO createRequest(UserPrincipal principal, AttendanceRequestDTO request) {
        User student = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student not found"));

        if (attendanceRequestRepository.existsByStudentIdAndDateAndStatusIn(
                student.getId(),
                request.getDate(),
                List.of(RequestStatus.PENDING, RequestStatus.APPROVED))) {
            throw new ApiException(HttpStatus.CONFLICT, "Attendance request already exists for the selected date");
        }

        AttendanceRequest attendanceRequest = new AttendanceRequest();
        attendanceRequest.setStudent(student);
        attendanceRequest.setDate(request.getDate());
        attendanceRequest.setReason(request.getReason());
        attendanceRequest.setStatus(RequestStatus.PENDING);
        return Mapper.toAttendanceRequestDto(attendanceRequestRepository.save(attendanceRequest));
    }

    public List<AttendanceRequestDTO> studentRequests(UserPrincipal principal) {
        return attendanceRequestRepository.findByStudentIdOrderByCreatedAtDesc(principal.getId()).stream()
                .map(Mapper::toAttendanceRequestDto)
                .toList();
    }

    public List<AttendanceRequestDTO> allPendingRequests() {
        return attendanceRequestRepository.findByStatusOrderByCreatedAtAsc(RequestStatus.PENDING).stream()
                .map(Mapper::toAttendanceRequestDto)
                .toList();
    }

    @Transactional
    public ApprovalResponseDTO approveRequest(Long requestId) {
        AttendanceRequest request = attendanceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Attendance request not found"));
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "Attendance request is already processed");
        }

        request.setStatus(RequestStatus.APPROVED);
        request.setApprovedAt(LocalDateTime.now());
        ensureAttendanceRecorded(request);
        attendanceRequestRepository.save(request);
        return new ApprovalResponseDTO("Attendance request approved");
    }

    @Transactional
    public ApprovalResponseDTO rejectRequest(Long requestId) {
        AttendanceRequest request = attendanceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Attendance request not found"));
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "Attendance request is already processed");
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setApprovedAt(LocalDateTime.now());
        attendanceRequestRepository.save(request);
        return new ApprovalResponseDTO("Attendance request rejected");
    }

    @Transactional
    @Scheduled(cron = "${app.attendance-requests.auto-approve-cron:0 0 1 * * *}", zone = "${app.timezone:Asia/Kolkata}")
    public void autoApprovePendingRequests() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(5);
        List<AttendanceRequest> requests = attendanceRequestRepository.findByStatusAndCreatedAtBefore(RequestStatus.PENDING, threshold);
        for (AttendanceRequest request : requests) {
            request.setStatus(RequestStatus.APPROVED);
            request.setApprovedAt(LocalDateTime.now());
            ensureAttendanceRecorded(request);
        }
        attendanceRequestRepository.saveAll(requests);
    }

    private void ensureAttendanceRecorded(AttendanceRequest request) {
        attendanceRecordRepository.findByStudentIdAndAttendanceDate(request.getStudent().getId(), request.getDate())
                .orElseGet(() -> {
                    AttendanceRecord record = new AttendanceRecord();
                    record.setStudent(request.getStudent());
                    record.setAttendanceDate(request.getDate());
                    record.setAttendanceType(AttendanceType.ONLINE);
                    return attendanceRecordRepository.save(record);
                });
    }
}
