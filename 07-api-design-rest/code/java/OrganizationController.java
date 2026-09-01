// System Design - Backend
// Chapter 07, API Design (REST) -> 09 Worked example: a PM platform
// Java 21 / Spring Boot 3.3

package com.example.rest.resource;

@RestController
@RequestMapping("/v1/organizations") // the collection URL, declared once
class OrganizationController {

    private final OrganizationService orgs;

    OrganizationController(OrganizationService orgs) {
        this.orgs = orgs;
    }

    // list + create share the collection URL, split by method
    @GetMapping
    List<Organization> list() {
        return orgs.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // 201 + the entity
    Organization create(@Valid @RequestBody CreateOrg in) {
        // a sane default: never make the client send the obvious
        String status = in.status() == null ? "active" : in.status();
        return orgs.insert(in.name(), status, in.description());
    }

    // get-one / update / delete share /{id}, split by method
    @GetMapping("/{id}")
    Organization get(@PathVariable String id) {
        return orgs.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204, nothing to say
    void delete(@PathVariable String id) {
        orgs.delete(id);
    }

    // a custom action: a verb at the end of one specific resource
    @PostMapping("/{id}/archive")
    Organization archive(@PathVariable String id) {
        // flips status and cascades: projects, tasks, emails...
        return orgs.archive(id); // a custom action returns 200, NOT 201
    }
}
