package eafit.caba_pro.service;

import eafit.caba_pro.model.Equipo;
import eafit.caba_pro.repository.EquipoRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EquipoService {
    private final EquipoRepository teamRepository;

    public EquipoService(EquipoRepository teamRepository){
        this.teamRepository = teamRepository;
    }

    @Transactional(readOnly = true)
    public List <Equipo> findAll(){
        return teamRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional <Equipo> findById(Long id){
        return teamRepository.findById(id);
    }

    @Transactional
    public void createTeam(Equipo team){
        if(team.getLogoUrl() == null)
            team.setLogoUrl("https://placehold.co/64x64");

        teamRepository.save(team);
    }

    @Transactional
    public void save(Equipo team){
        teamRepository.save(team);
    }

    
}

