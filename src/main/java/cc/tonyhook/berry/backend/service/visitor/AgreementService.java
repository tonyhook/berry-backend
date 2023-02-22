package cc.tonyhook.berry.backend.service.visitor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cc.tonyhook.berry.backend.dao.visitor.AgreementRepository;
import cc.tonyhook.berry.backend.entity.visitor.Agreement;
import jakarta.transaction.Transactional;

@Service
public class AgreementService {

    @Autowired
    private AgreementRepository agreementRepository;

    public Agreement getLatestAgreement(String name) {
        Agreement agreement = agreementRepository.findTopByNameOrderByVersionDesc(name);

        return agreement;
    }

    public Agreement getAgreement(String name, Integer version) {
        Agreement agreement = agreementRepository.findByNameAndVersion(name, version);

        return agreement;
    }

    public List<Agreement> getAgreementList() {
        List<Agreement> agreementList = agreementRepository.findAll();

        return agreementList;
    }

    public Agreement getAgreement(Integer id) {
        Agreement agreement = agreementRepository.findById(id).orElse(null);

        return agreement;
    }

    public Agreement addAgreement(Agreement newAgreement) {
        Agreement updatedAgreement = agreementRepository.save(newAgreement);

        return updatedAgreement;
    }

    public void updateAgreement(Integer id, Agreement newAgreement) {
        agreementRepository.save(newAgreement);
    }

    @Transactional
    public void removeAgreement(Integer id) {
        Agreement deletedAgreement = agreementRepository.findById(id).orElse(null);

        agreementRepository.delete(deletedAgreement);
    }

}
