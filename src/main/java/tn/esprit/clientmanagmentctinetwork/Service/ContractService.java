package tn.esprit.clientmanagmentctinetwork.Service;

import tn.esprit.clientmanagmentctinetwork.Dto.ContractDto;
import tn.esprit.clientmanagmentctinetwork.Model.ContractModel;
import java.util.List;

public interface ContractService {
    ContractModel createContract(ContractDto dto);
    List<ContractModel> getContractsByClientId(Long clientId);
    void deleteContract(Long id);
    List<ContractModel> searchAndFilterContracts(String keyword, String redevance);
    ContractModel updateContractSchedule(Long id, String months);
    void checkContractNotifications();
    ContractModel getContractById(Long id);
    ContractModel updateContract(Long id, ContractDto dto);
    ContractModel updateStatus(Long id, String status);
    ContractModel renewContract(Long id);
}