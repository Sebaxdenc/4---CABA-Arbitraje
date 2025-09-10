package eafit.caba_pro.service;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;
import eafit.caba_pro.model.Equipo;
import eafit.caba_pro.repository.EquipoRepository;

@Service
public class EquipoService {

    private final EquipoRepository equipoRepository;   
    
    public EquipoService(EquipoRepository equipoRepository){
        this.equipoRepository = equipoRepository;
    }

    @Transactional(readOnly = true)
    public List <Equipo> findAll(){
        return equipoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional <Equipo> findById(Long id){
        return equipoRepository.findById(id);
    }

    @Transactional
    public void createTeam(Equipo team){
        if(team.getLogo() == null)
            team.setLogo("https://placehold.co/64x64");

        equipoRepository.save(team);
    }

    @Transactional
    public void save(Equipo team){
        equipoRepository.save(team);
    }
}

