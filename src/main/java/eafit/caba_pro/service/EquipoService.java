package eafit.caba_pro.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eafit.caba_pro.model.Equipo;
import eafit.caba_pro.repository.EquipoRepository;

@Service
public class EquipoService {

    @Autowired
    EquipoRepository equipoRepository;    

    private final EquipoRepository repo;
    public EquipoService(EquipoRepository repo) { this.repo = repo; }

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

