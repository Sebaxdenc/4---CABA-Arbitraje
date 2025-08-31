package eafit.caba_pro.service;

import eafit.caba_pro.model.Team;
import eafit.caba_pro.repository.TeamRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeamService {
    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository){
        this.teamRepository = teamRepository;
    }

    @Transactional(readOnly = true)
    public List <Team> findAll(){
        return teamRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional <Team> findById(Long id){
        return teamRepository.findById(id);
    }

    @Transactional
    public void createTeam(Team team){
        if(team.getState() == null)
            team.setState(true);

        if(team.getLogo() == null)
            team.setLogo("https://placehold.co/64x64");

        teamRepository.save(team);
    }

    @Transactional
    public void save(Team team){
        teamRepository.save(team);
    }

    
}

