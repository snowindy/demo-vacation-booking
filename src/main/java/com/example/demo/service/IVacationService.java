package com.example.demo.service;

import java.util.List;

import com.example.demo.model.VacationRequest;

public interface IVacationService {
     List<VacationRequest> getEmployeeRequests(Long employeeId);
    List<VacationRequest> getEmployeeRequestsByStatus(Long employeeId, VacationRequest.Status status);
    int getRemainingVacationDays(Long employeeId);
    VacationRequest createVacationRequest(VacationRequest request);

    // Manager-related methods
    List<VacationRequest> getAllRequests();
    List<VacationRequest> getRequestsByStatus(VacationRequest.Status status);
    List<VacationRequest> getRequestsByEmployee(Long employeeId);
    List<VacationRequest> getOverlappingRequests(VacationRequest request);
    VacationRequest processRequest(Long requestId, VacationRequest.Status status, String comment, Long managerId);
}
