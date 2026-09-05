// System Design - Backend
// Chapter 07, API Design (REST) -> 07 List APIs: page / sort / filter
// Java 21 / Spring Boot 3.3

package com.example.rest.listing;

@RestController
class OrganizationListController {
    
    // GET /v1/organizations?status=active&sort=name,asc&page=0&size=10
    @GetMapping("/v1/organizations")
    ListResponse<Organization> list(
        @RequestParam(required = false) String status,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        // Spring parses page, size and sort straight out of the query
        // string, so the "sane defaults" are declared rather than coded.
        Page<Organization> page = (status == null)
                                      ? orgs.findAll(pageable)
                                      // ?status=active
                                      : orgs.findByStatus(status, pageable);
        
        return new ListResponse<>(
            page.getContent(),
            new Pagination(page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages()));
    }
    // Spring runs the count query and works out totalPages itself, so
    // the ceil-division that every hand-rolled pager gets wrong once is
    // simply not there to get wrong.
}
