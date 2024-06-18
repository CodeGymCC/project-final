package com.codegym.jira.bugtracking.project;

import com.codegym.jira.bugtracking.Handlers;
import com.codegym.jira.bugtracking.ObjectType;
import com.codegym.jira.common.BaseHandler;
import com.codegym.jira.bugtracking.project.to.ProjectTo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.codegym.jira.common.BaseHandler.createdResponse;

@RestController
@RequestMapping(value = BaseHandler.REST_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ProjectController {
    private final Handlers.ProjectHandler handler;

    @GetMapping("/projects")
    public List<ProjectTo> getAll() {
        return handler.getAllTos(ProjectRepository.NEWEST_FIRST);
    }

    @GetMapping("/projects/{id}")
    public ProjectTo getById(@PathVariable Long id) {
        return handler.getTo(id);
    }

    @PostMapping(path = "/mngr/projects", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Project> create(@Valid @RequestBody ProjectTo projectTo) {
        Project created = handler.createWithBelong(projectTo, ObjectType.PROJECT, "project_author");
        return BaseHandler.createdResponse(BaseHandler.REST_URL + "/projects", created);
    }

    @PutMapping("/mngr/projects/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@Valid @RequestBody ProjectTo projectTo, @PathVariable Long id) {
        handler.updateFromTo(projectTo, id);
    }

    @PatchMapping("/mngr/projects/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enable(@PathVariable long id, @RequestParam boolean enabled) {
        handler.enable(id, enabled);
    }
}
