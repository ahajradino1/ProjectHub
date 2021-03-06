package ba.unsa.etf.nwt.projectservice.projectservice.service;

import ba.unsa.etf.nwt.projectservice.projectservice.client.dto.UserDTO;
import ba.unsa.etf.nwt.projectservice.projectservice.dto.ProjectCollaboratorDTO;
import ba.unsa.etf.nwt.projectservice.projectservice.exception.base.ForbiddenException;
import ba.unsa.etf.nwt.projectservice.projectservice.exception.base.NotFoundException;
import ba.unsa.etf.nwt.projectservice.projectservice.exception.base.UnprocessableEntityException;
import ba.unsa.etf.nwt.projectservice.projectservice.model.Project;
import ba.unsa.etf.nwt.projectservice.projectservice.model.ProjectCollaborator;
import ba.unsa.etf.nwt.projectservice.projectservice.repository.ProjectCollaboratorRepository;
import ba.unsa.etf.nwt.projectservice.projectservice.security.ResourceOwner;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectCollaboratorService {
    private final ProjectCollaboratorRepository projectCollaboratorRepository;


    public ProjectCollaborator createCollaborator(ResourceOwner resourceOwner, UUID collaboratorId, Project project) {
        if (resourceOwner.getId().equals(collaboratorId)) {
            throw new UnprocessableEntityException("You are already a collaborator on this project");
        }        projectCollaboratorRepository.findByCollaboratorIdAndProjectId(collaboratorId, project.getId()).ifPresent(pc -> {
            throw new UnprocessableEntityException("Collaborator already added to this project");
        });

        ProjectCollaborator projectCollaborator = new ProjectCollaborator();
        projectCollaborator.setProject(project);
        projectCollaborator.setCollaboratorId(collaboratorId);
        return projectCollaboratorRepository.save(projectCollaborator);
    }

    public ProjectCollaborator findByCollaboratorIdAndProjectId(UUID collaboratorId, UUID projectId) {
        return projectCollaboratorRepository.findByCollaboratorIdAndProjectId(collaboratorId, projectId).orElseThrow(() -> {
            throw new NotFoundException("Collaborator not found");
        });
    }

    public boolean existsByCollaboratorIdAndProjectId(UUID userId, UUID projectId) {
        return projectCollaboratorRepository.findByCollaboratorIdAndProjectId(userId, projectId).isEmpty();
    }

    public void delete(ProjectCollaborator projectCollaborator) {
        projectCollaboratorRepository.delete(projectCollaborator);
    }

    public Page<ProjectCollaboratorDTO> getCollaboratorsForProject(Project project, Pageable pageable) {
        return projectCollaboratorRepository.findAllByProject(project, pageable);
    }

    public void checkIfOwnerOrCollaborator(UUID ownerId, UUID resourceOwnerId, UUID projectId) {
        if (!ownerId.equals(resourceOwnerId) &&
                existsByCollaboratorIdAndProjectId(resourceOwnerId, projectId))
            throw new ForbiddenException("You don't have permission for this activity");
    }

    public void checkIfCollaboratorExists(UUID ownerId, UUID collaboratorId, UUID projectId) {
        if (!ownerId.equals(collaboratorId) &&
                existsByCollaboratorIdAndProjectId(collaboratorId, projectId))
            throw new NotFoundException("Collaborator not found");
    }

    public Page<ProjectCollaboratorDTO> addElementToPage(Page<ProjectCollaboratorDTO> collaboratorPage, Project project, UserDTO userDTO, final Pageable pageable) {
        ProjectCollaborator owner  = new ProjectCollaborator();
        owner.setId(UUID.randomUUID());
        owner.setProject(project);
        owner.setCollaboratorId(project.getOwnerId());
        ProjectCollaboratorDTO ownerDTO = new ProjectCollaboratorDTO(owner);
        ownerDTO.setCollaborator(userDTO);
        List<ProjectCollaboratorDTO> projectCollaboratorDTOS = new ArrayList<>();
        collaboratorPage.forEach(projectCollaboratorDTOS::add);
        projectCollaboratorDTOS.add(0, ownerDTO);
        long total = countCollaboratorsForProject(project);
        return new PageImpl<>(projectCollaboratorDTOS, pageable, total);
    }

    public long countCollaboratorsForProject(final Project project) {
        return projectCollaboratorRepository.countAllByProject(project);
    }
}
