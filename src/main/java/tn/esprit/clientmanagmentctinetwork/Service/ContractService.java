package tn.esprit.clientmanagmentctinetwork.Service;

import tn.esprit.clientmanagmentctinetwork.Dto.ContractDto;
import tn.esprit.clientmanagmentctinetwork.Dto.VisitValidationDto;
import tn.esprit.clientmanagmentctinetwork.Model.ContractModel;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ContractService {
    ContractModel createContract(ContractDto dto);
    List<ContractModel> getContractsByClientId(Long clientId);
    void deleteContract(Long id);
    List<ContractModel> getMonthlySchedules(int month, int year);
    List<ContractModel> searchAndFilterContracts(String keyword, String redevance, String status);

    // ADDED: Fetch contract by ID for the REST controller
    ContractModel getContractById(Long id);

    // Modern date-based updaters, state toggles, and renewals [1.1.4, 1.2.1]
    ContractModel updateContract(Long id, ContractDto dto);
    ContractModel updateStatus(Long id, String status);
    ContractModel renewContract(Long id);
    ContractModel updateContractScheduleDates(Long id, List<tn.esprit.clientmanagmentctinetwork.Dto.VisitScheduleDto> visits);
    void checkContractNotifications();

    ContractModel validateVisit(Long id, VisitValidationDto dto, String currentUserName);
    ContractModel deleteVisitData(Long id, int index);
    List<Map<String, Object>> getUrgentVisits();

}